package io.jpm.core.jpm_repository.parse.infra.deprecated;

import io.jpm.api.MqAssociation;
import io.jpm.api.MqCollection;
import com.sun.source.tree.*;
import com.sun.source.util.Trees;
import io.jpm.config.AppConfig;
import io.jpm.core.jpm_repository.parse.domain.vo.*;

import io.jpm.core.jpm_repository.valid.policy.ArgValidatorPolicy;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.common.utils.LogPrinter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Field;
import java.util.*;


@Deprecated
public class MqRepoParserV2 {

    private static final Set<String> DSL_KEYWORDS = new HashSet<>(Arrays.asList(
            "select", "from", "where", "and", "or", "andGroup", "orGroup", "endGroup",
            "innerJoin", "leftJoin", "hashJoin", "mergeJoin", "loopJoin",
            "insertInto", "update", "deleteFrom", "value", "set", "setRaw",
            "orderBy", "groupBy", "limit", "offset", "sql", "selectRaw", "orderByRaw", "groupByRaw",
            "whereInGroup", "group", "fromGroup", "selectCase",
            "mapTarget", "mapId", "mapResult", "mapJoin", "innerJoinGroup", "leftJoinGroup",
            "whereExistsGroup", "whereNotExistsGroup"
    ));


    private static final RepoMetaRegistry REPO_META_REGISTRY = AppConfig.getEntityMetaRegistry();

    /**
     * 메인 진입점: 어노테이션 프로세서에서 찾은 TypeElement를 분석합니다.
     */
    public static RepoMeta parseRepo(TypeElement repoElement, ProcessingEnvironment env, Trees trees) {
        String className = repoElement.getSimpleName().toString();
        String namespace = extractNamespace(repoElement, className);

        RepoMeta repoMeta = new RepoMeta(className, namespace);
        ClassTree classTree = trees.getTree(repoElement);


        if (classTree == null) return repoMeta;

        // 1. 클래스 내부 메서드 순회
        for (Tree member : classTree.getMembers()) {
            if (member instanceof MethodTree) {
                MethodTree methodTree = (MethodTree) member;
                 MethodMeta methodMeta = new MethodMeta(methodTree.getName().toString());
                Map<String, String> argContext = new HashMap<>();

                // 파라미터 추출
                for (VariableTree param : methodTree.getParameters()) {
                    String paramName = param.getName().toString();

                    String paramType = param.getType().toString();
                    methodMeta.addParameter(paramName, paramType);
                    argContext.put("isParam_" + paramName, "true");
                    argContext.put("paramType_" + paramName, paramType);
                }

                // 메서드 바디 내부 구문 분석
                BlockTree body = methodTree.getBody();

                if (body != null) {
                    for (StatementTree stmt : body.getStatements()) {
                        if (stmt instanceof ExpressionStatementTree) {
                            ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
                            // 체이닝된 메서드들(.select().from()...)을 리스트로 평탄화 (실행 순서대로)
                            List<MethodInvocationTree> chain = flattenMethodChains(expr);

                            for (MethodInvocationTree call : chain) {
                                String command = getMethodName(call);
                                String scopeName = getScopeName(call);


                                LogPrinter.info("call: " + call.getMethodSelect());
                                if (DSL_KEYWORDS.contains(command)) {
                                    List<String> rawArgs = extractTokensTree(call, argContext, methodMeta);
                                    processDslCommand(command, rawArgs, methodMeta, argContext);
                                } else if (scopeName != null && scopeName.contains("segment")) {
                                    List<String> passedArgs = extractTokensTree(call, argContext, methodMeta);
                                    // 파일 경로 대신 ProcessingEnvironment를 넘겨서 Tree API로 세그먼트를 찾습니다.

                                    LogPrinter.info("[1] scopeName=" + scopeName + " command=" + command);
                                    LogPrinter.info("[1] passedArgs=" + passedArgs); // 비어있으면 extractTokensTree 문제

                                    String segmentFqcn = REPO_META_REGISTRY.getSegmentPath(className, scopeName);
                                    LogPrinter.info("[1] segmentFqcn=" + segmentFqcn); // null이면 등록 문제
                                    if (segmentFqcn != null) {
                                        inlineSegmentMethodTree(env, trees, className, scopeName, command, methodMeta, passedArgs);
                                    }
                                }
                            }
                        }
                    }
                }
                LogPrinter.info("[METHOD CHECK] name=" + methodTree.getName()
                        + " statements=" + methodMeta.getStatements().size()
                        + " params=" + methodMeta.getParameters().size());



                if (!methodMeta.getStatements().isEmpty()) {
                    LogPrinter.info("[METHOD CHECK] statements=" + methodMeta.getStatements().size());
                    repoMeta.addMethod(methodMeta);
                }
            }
        }


        return repoMeta;
    }

    /**
     * @JpmRepository 또는 @MqRepository의 name 값을 추출합니다.
     */
    private static String extractNamespace(TypeElement repoElement, String defaultName) {
        for (AnnotationMirror mirror : repoElement.getAnnotationMirrors()) {
            String annoName = mirror.getAnnotationType().asElement().getSimpleName().toString();
            if (annoName.equals("JpmRepository") || annoName.equals("MqRepository")) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().toString().equals("name")) {
                        String value = entry.getValue().getValue().toString();
                        if (!value.trim().isEmpty()) return value;
                    }
                }
            }
        }
        return defaultName;
    }

    /**
     * Tree API로 Segment 인라인 처리
     */
    private static void inlineSegmentMethodTree(ProcessingEnvironment env, Trees trees,
                                                String repoClassName, String fieldVarName, String segmentMethodName,
                                                MethodMeta methodMeta, List<String> passedArgs) {

        String segmentTypeName = REPO_META_REGISTRY.getSegmentPath(repoClassName, fieldVarName);// 클래스 풀네임 반환한다고 가정



        if (segmentTypeName == null) return;

        TypeElement segmentElement = env.getElementUtils().getTypeElement(segmentTypeName);
        if (segmentElement == null) return;

        ClassTree segmentTree = trees.getTree(segmentElement);
        if (segmentTree == null) return;

        for (Tree member : segmentTree.getMembers()) {
            if (member instanceof MethodTree) {
                MethodTree methodTree = (MethodTree) member;
                if (methodTree.getName().toString().equals(segmentMethodName)) {
                    Map<String, String> argContext = new HashMap<>();
                    for (int i = 0; i < methodTree.getParameters().size(); i++) {
                        if (i < passedArgs.size()) {
                            String paramName = methodTree.getParameters().get(i).getName().toString();
                            argContext.put(paramName, passedArgs.get(i));
                            LogPrinter.info("[2] argContext 매핑: " + paramName + " → " + passedArgs.get(i));
                        }
                    }

                    BlockTree body = methodTree.getBody();
                    if (body != null) {
                        for (StatementTree stmt : body.getStatements()) {
                            if (stmt instanceof ExpressionStatementTree) {
                                ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
                                List<MethodInvocationTree> chain = flattenMethodChains(expr);
                                for (MethodInvocationTree call : chain) {
                                    String command = getMethodName(call);
                                    if (DSL_KEYWORDS.contains(command)) {
                                        List<String> rawArgs = extractTokensTree(call, argContext, methodMeta);
                                        processDslCommand(command, rawArgs, methodMeta, argContext);
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * 메서드 체이닝 (a.select().from().where()) 을 실행 순서(왼쪽->오른쪽) 리스트로 변환합니다.
     */
    private static List<MethodInvocationTree> flattenMethodChains(ExpressionTree expr) {
        List<MethodInvocationTree> chains = new ArrayList<>();
        ExpressionTree current = expr;
        while (current instanceof MethodInvocationTree) {
            chains.add((MethodInvocationTree) current);
            ExpressionTree methodSelect = ((MethodInvocationTree) current).getMethodSelect();
            if (methodSelect instanceof MemberSelectTree) {
                current = ((MemberSelectTree) methodSelect).getExpression();
            } else {
                break;
            }
        }
        Collections.reverse(chains); // 최하위 노드가 먼저 실행되도록 순서 뒤집기
        return chains;
    }

    private static String getMethodName(MethodInvocationTree call) {
        ExpressionTree methodSelect = call.getMethodSelect();
        if (methodSelect instanceof IdentifierTree) return ((IdentifierTree) methodSelect).getName().toString();
        if (methodSelect instanceof MemberSelectTree) return ((MemberSelectTree) methodSelect).getIdentifier().toString();
        return "";
    }

    private static String getScopeName(MethodInvocationTree call) {
        ExpressionTree methodSelect = call.getMethodSelect();
        if (methodSelect instanceof MemberSelectTree) {
            ExpressionTree scope = ((MemberSelectTree) methodSelect).getExpression();
            if (scope instanceof IdentifierTree) return ((IdentifierTree) scope).getName().toString();
            if (scope instanceof MemberSelectTree) return ((MemberSelectTree) scope).getIdentifier().toString();
        }
        return null;
    }

    private static List<String> extractTokensTree(MethodInvocationTree call, Map<String, String> argContext, MethodMeta methodMeta) {
        try {
            List<String> args = new ArrayList<>();
            String command = getMethodName(call);
            boolean isCondition = Arrays.asList("where", "and", "or").contains(command);


            List<? extends ExpressionTree> arguments = call.getArguments();

            ArgValidatorPolicy argValidatorPolicy = new ArgValidatorPolicy();
            for (int i = 0; i < arguments.size(); i++) {
                ExpressionTree arg = arguments.get(i);
                boolean quoteString = isCondition && i == 2;


                String column = resolveValueTree(arguments.get(0), argContext, false, false);
                argValidatorPolicy.saveFirstArgInfoIfMatched(column, command, i);
                argValidatorPolicy.validateArgIfMatched(i, lastArgToString(arg));

                // ✅ resolveToColumn=false → MemberReferenceTree를 className::method 원본 그대로 유지
                String resolved = resolveValueTree(arg, argContext, quoteString, false);

                // ✅ :: 후처리 완전 제거 - SqlMapperBinderImpl이 처리
                args.add(resolved != null ? resolved : "");
            }
            return args;
        }catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }





    private static String resolveValueTree(ExpressionTree expr, Map<String, String> argContext, boolean quoteString, boolean resolveToColumn) {
        if (expr == null) return "";
        try {
            if (expr instanceof MethodInvocationTree) {
                MethodInvocationTree mCall = (MethodInvocationTree) expr;
                String name = getMethodName(mCall);
                List<? extends ExpressionTree> args = mCall.getArguments();

                if ("raw".equals(name) && !args.isEmpty()) {
                    return resolveValueTree(args.get(0), argContext, false, resolveToColumn);
                }
                if ("quoted".equals(name) && !args.isEmpty()) {
                    String inner = resolveValueTree(args.get(0), argContext, false, true);
                    if (inner.contains("::")) inner = extractFieldNameFromMethodRef(inner);
                    return "'" + inner + "'";
                }
                if ("bind".equals(name) && !args.isEmpty()) {
                    String inner = resolveValueTree(args.get(0), argContext, false, true);
                    if (inner.contains("::")) inner = extractFieldNameFromMethodRef(inner);
                    return "#{" + inner + "}";
                }
                if ("as".equals(name) && !args.isEmpty()) {
                    ExpressionTree methodSel = mCall.getMethodSelect();
                    if (!(methodSel instanceof MemberSelectTree)) return "";
                    ExpressionTree scope = ((MemberSelectTree) methodSel).getExpression();
                    return resolveValueTree(scope, argContext, false, true)
                            + " AS "
                            + resolveValueTree(args.get(0), argContext, false, true);
                }
                if ("col".equals(name) && args.size() >= 2) {
                    String alias = resolveValueTree(args.get(0), argContext, false, true);
                    String field = resolveValueTree(args.get(1), argContext, false, resolveToColumn);
                    /*if (field.contains("::")) field = extractFieldNameFromMethodRef(field);*/

                    LogPrinter.info("[joinNode] alias : " + alias + " field : " + field);
                    return alias + "." + field;
                }
            }

            if (expr instanceof LiteralTree) {
                Object val = ((LiteralTree) expr).getValue();
                if (val instanceof Boolean) return ((Boolean) val) ? "TRUE" : "FALSE";
                if (val instanceof String) return quoteString ? "'" + val + "'" : val.toString();
                return val != null ? val.toString() : "";
            }

            if (expr instanceof MemberReferenceTree) {
                MemberReferenceTree mRef = (MemberReferenceTree) expr;
                String className = mRef.getQualifierExpression().toString();
                String methodName = mRef.getName().toString();
                String fieldName = convertGetterToField(methodName);

                // join 조건 컬럼은 className::methodName 그대로 유지
                if (!resolveToColumn) {
                    return className + "::" + methodName;
                }

                EntityMeta entityMeta = REPO_META_REGISTRY.getEntityMeta(className);
                if (entityMeta != null) {
                    String columnName = entityMeta.getColumn(fieldName);
                    if (columnName != null) return columnName;
                    return fieldName;
                }


                return className + "::" + methodName;
            }

            if (expr instanceof IdentifierTree) {
                String name = ((IdentifierTree) expr).getName().toString();
                LogPrinter.info("[3] IdentifierTree name=" + name + " argContext keys=" + argContext.keySet());

                if (argContext.containsKey(name)) {
                    String val = argContext.get(name);
                    return val != null ? val : name;
                }
                if (argContext.containsKey("isParam_" + name)) return "#{" + name + "}";
                if (argContext.containsKey("alias_" + name)) return argContext.get("alias_" + name);
                return name;
            }


        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
        }
        return expr.toString().replace("\"", "");
    }

    private static void processDslCommand(String command, List<String> rawArgs, MethodMeta methodMeta, Map<String, String> argContext) {
        if ("mapJoin".equals(command)) {
            if (rawArgs.isEmpty()) return;
            String raw = rawArgs.get(0);
            String fieldName = extractFieldNameFromMethodRef(raw);
            String alias = rawArgs.size() > 1 ? rawArgs.get(1) : null;

            MapJoinMeta.MappingType mappingType = resolveMappingType(raw, fieldName);
            methodMeta.addMapJoin(new MapJoinMeta(fieldName, alias, mappingType));
            methodMeta.addStatement(new DslStatement(command, rawArgs));
        } else if (Arrays.asList("innerJoin", "leftJoin", "hashJoin", "mergeJoin", "loopJoin").contains(command)) {
            List<String> joinArgs = new ArrayList<>();
            if (!rawArgs.isEmpty()) joinArgs.add(rawArgs.get(0));
            if (rawArgs.size() > 1) joinArgs.add(rawArgs.get(1));
            if (rawArgs.size() > 2) joinArgs.add(rawArgs.get(2));

            String extractedAlias = "";
            if (rawArgs.size() > 2 && rawArgs.get(2).contains("|")) {
                extractedAlias = rawArgs.get(2).split("\\|")[0];
            }
            while (joinArgs.size() < 4) joinArgs.add("");
            joinArgs.add(extractedAlias);

            methodMeta.addStatement(new DslStatement(command, joinArgs));
        } else {
            methodMeta.addStatement(new DslStatement(command, rawArgs));


            if (Arrays.asList("from", "insertInto", "update", "deleteFrom").contains(command) && !rawArgs.isEmpty()) {
                LogPrinter.info("[MqRepoParser] methodMeta name = "+rawArgs+" [MqRepoParser] rawArgs = " + rawArgs);
                methodMeta.setTargetType(rawArgs.get(0).replace(".class", ""));
            } else if ("mapTarget".equals(command) && !rawArgs.isEmpty()) {
                methodMeta.setTargetType(rawArgs.get(0).replace(".class", ""));
            }
        }
    }


    private static String extractFieldNameFromMethodRef(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String cleaned = raw.trim();
        if (cleaned.contains("|")) {
            String[] pipeParts = cleaned.split("\\|");
            cleaned = pipeParts[pipeParts.length - 1];
        }
        if (cleaned.contains("::")) {
            return convertGetterToField(cleaned.split("::")[1].trim());
        }
        return cleaned;
    }

    private static String convertGetterToField(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
        return methodName;
    }

    private static MapJoinMeta.MappingType resolveMappingType(String raw, String fieldName) {
        String classNamePart = raw.contains("|")
                ? raw.split("\\|")[1].split("::")[0].trim()
                : raw.contains("::") ? raw.split("::")[0].trim() : null;

        if (classNamePart == null) return MapJoinMeta.MappingType.AUTO;

        try {
            EntityMeta meta = REPO_META_REGISTRY.getEntityMeta(classNamePart);
            if (meta == null) return MapJoinMeta.MappingType.AUTO;

            Class<?> entityClass = REPO_META_REGISTRY.getEntityClass(classNamePart);
            Field field = entityClass.getDeclaredField(fieldName);

            if (field.isAnnotationPresent(MqCollection.class)) return MapJoinMeta.MappingType.COLLECTION;
            if (field.isAnnotationPresent(MqAssociation.class)) return MapJoinMeta.MappingType.ASSOCIATION;

            return List.class.isAssignableFrom(field.getType())
                    ? MapJoinMeta.MappingType.COLLECTION
                    : MapJoinMeta.MappingType.ASSOCIATION;
        } catch (Exception e) {

            LogPrinter.exceptionInfo(e);

            return MapJoinMeta.MappingType.AUTO;
        }
    }


    private static String lastArgToString(ExpressionTree lastArg)
    {
        String lastArgFieldType = null;
        if (lastArg instanceof LiteralTree) {
            Object val = ((LiteralTree) lastArg).getValue();
            if (val instanceof String) lastArgFieldType = inferTypeFromString(val.toString());
            else if (val instanceof Boolean) lastArgFieldType = "BOOLEAN";
            else if (val instanceof Integer) lastArgFieldType = "INTEGER";
            else if (val instanceof Long) lastArgFieldType = "LONG";
            else if (val instanceof Double || val instanceof Float) lastArgFieldType = "DOUBLE";
        }
        return  lastArgFieldType;
    }



    private static String inferTypeFromString(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) return "UNKNOWN";

        String trimmed = rawValue.trim();

        // 1. 따옴표로 감싸진 리터럴 체크 (최우선순위)
        // 'abc', '', '123' 등 따옴표가 시작과 끝에 있다면 무조건 STRING입니다.
        if (trimmed.startsWith("'") && trimmed.endsWith("'")) {
            return "STRING";
        }

        // 만약 "a = ''" 전체가 들어오는 경우를 대비해 따옴표가 포함되어 있는지 확인
        if (trimmed.contains("'")) {
            return "STRING";
        }

        // 2. 불리언 체크
        if (trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("false")) {
            return "BOOLEAN";
        }

        // 3. 변수명과 연산자 제거 (숫자 리터럴만 남기기)
        // [a-zA-Z_][a-zA-Z0-9_]* : 영문으로 시작하는 변수/필드명 제거
        // [+/*%()-] : 산술 연산자 및 괄호 제거
        String onlyLiterals = trimmed.replaceAll("[a-zA-Z_][a-zA-Z0-9_]*", "")
                .replaceAll("[+/*%()-]", "")
                .trim();

        // 4. 다 지웠는데 아무것도 없다면? (ex: col_a + col_b) -> 숫자형 수식으로 간주
        if (onlyLiterals.isEmpty()) {
            return "NUMERIC_EXPRESSION";
        }

        // 5. 남은 리터럴에 소수점이 있으면 DOUBLE
        if (onlyLiterals.contains(".")) {
            return "DOUBLE";
        }

        // 6. 남은 게 숫자라면 INTEGER/LONG
        if (onlyLiterals.matches(".*\\d+.*")) {
            return "INTEGER"; // 수식 내 숫자는 기본적으로 정수형으로 취급
        }

        return "STRING";
    }


}