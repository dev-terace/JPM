package mq_mapper.infra.ast.prev;

import annotation.MqAssociation;
import annotation.MqCollection;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import config.AppConfig;
import mq_mapper.domain.vo.*;
import mq_mapper.infra.repo.EntityMetaRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.*;

// 테스트 코드용

@Deprecated
public class MqRepoParser {

    private static final Set<String> DSL_KEYWORDS = new HashSet<>(Arrays.asList(
            // 기존 SQL 키워드
            "select", "from", "where", "and", "or", "andGroup", "orGroup", "endGroup",
            "innerJoin", "leftJoin", "hashJoin", "mergeJoin", "loopJoin",
            "insertInto", "update", "deleteFrom", "value", "set", "setRaw",
            "orderBy", "groupBy", "limit", "offset", "sql", "selectRaw", "orderByRaw", "groupByRaw",
            "whereInGroup", "group", "fromGroup",

            "selectCase", // 🚀 [추가] CASE 문법을 파서가 인식하도록 추가!


            // 신규 매핑 키워드 추가
            "mapTarget", "mapId", "mapResult", "mapJoin", "innerJoinGroup", "leftJoinGroup", "whereExistsGroup",
            "whereNotExistsGroup"

    ));

    private static final EntityMetaRegistry entityMetaRegistry = AppConfig.getEntityMetaRegistry();

    public static Map<String, RepoMeta> parseFile(String filePath) {
        Map<String, RepoMeta> repoMap = new LinkedHashMap<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("File not found: " + filePath);
            return repoMap;
        }

        try (FileInputStream in = new FileInputStream(file)) {
            CompilationUnit cu = StaticJavaParser.parse(in);

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                String className = classDecl.getNameAsString();
                String namespace = className;



                // ★ @JpmRepository 또는 @MqRepository 둘 다 호환되도록 수정
                Optional<AnnotationExpr> annotationOpt = classDecl.getAnnotationByName("JpmRepository");
                if (!annotationOpt.isPresent()) {
                    annotationOpt = classDecl.getAnnotationByName("MqRepository");
                }

                if (annotationOpt.isPresent()) {
                    AnnotationExpr annotation = annotationOpt.get();
                    if (annotation.isNormalAnnotationExpr()) {
                        NormalAnnotationExpr normalExpr = annotation.asNormalAnnotationExpr();
                        for (MemberValuePair pair : normalExpr.getPairs()) {
                            if ("name".equals(pair.getNameAsString()) && pair.getValue().isStringLiteralExpr()) {
                                String extractedName = pair.getValue().asStringLiteralExpr().getValue();
                                if (extractedName != null && !extractedName.trim().isEmpty()) {
                                    namespace = extractedName;
                                }
                            }
                        }
                    }
                }

                RepoMeta repoMeta = new RepoMeta(className, namespace);

                classDecl.findAll(MethodDeclaration.class).forEach(method -> {
                    MethodMeta methodMeta = new MethodMeta(method.getNameAsString());

                    Map<String, String> argContext = new HashMap<>();

                    method.getParameters().forEach(param -> {
                        String paramType = param.getTypeAsString();
                        String paramName = param.getNameAsString();
                        methodMeta.addParameter(paramName, paramType);
                        argContext.put("isParam_" + param.getNameAsString(), "true");
                        argContext.put("paramType_" + paramName, paramType);
                    });

                    method.getBody().ifPresent(body -> {
                        body.findAll(ExpressionStmt.class).forEach(stmt -> {
                            if (stmt.getExpression().isMethodCallExpr()) {
                                MethodCallExpr call = stmt.getExpression().asMethodCallExpr();
                                String command = call.getNameAsString();
                                Optional<Expression> scope = call.getScope();

                                if (DSL_KEYWORDS.contains(command)) {
                                    List<String> rawArgs = extractTokens(call, cu, argContext, methodMeta);
                                    processDslCommand(command, rawArgs, methodMeta, argContext);

                                }else if (scope.isPresent() && scope.get().toString().startsWith("segment")) {


                                    List<String> passedArgs = extractTokens(call, cu, argContext, methodMeta);


                                    inlineSegmentMethod(filePath, command, methodMeta, passedArgs);
                                }


                            }
                        });
                    });

                    if (!methodMeta.getStatements().isEmpty() || !methodMeta.getParameters().isEmpty()) {
                        repoMeta.addMethod(methodMeta);
                    }
                });

                repoMap.put(className, repoMeta);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return repoMap;
    }








    private static void inlineSegmentMethod(String repoPath, String segmentMethodName, MethodMeta methodMeta, List<String> passedArgs) {
        // 1. Registry에서 이 레포지토리에 연결된 세그먼트 파일 경로를 가져옴
        String segmentPath = entityMetaRegistry.getSegmentPath(repoPath, segmentMethodName);

        if (segmentPath == null) {
            System.err.println("경고: " + repoPath + "에 연결된 세그먼트 경로가 없습니다.");
            return;
        }



        try (FileInputStream in = new FileInputStream(new File(segmentPath))) {
            CompilationUnit segmentCu = StaticJavaParser.parse(in);

            // 2. 세그먼트 파일 내에서 호출된 메서드(segmentMethodName)를 찾음
            segmentCu.findAll(MethodDeclaration.class).stream()
                    .filter(m -> m.getNameAsString().equals(segmentMethodName))
                    .findFirst()
                    .ifPresent(m -> {
                        // 🚀 핵심: 파라미터 이름과 전달된 값을 매핑 (예: alias -> "o")
                        Map<String, String> argContext = new HashMap<>();
                        for (int i = 0; i < m.getParameters().size(); i++) {
                            if (i < passedArgs.size()) {
                                argContext.put(m.getParameter(i).getNameAsString(), passedArgs.get(i));
                            }
                        }

                        m.getBody().ifPresent(body -> {
                            body.findAll(MethodCallExpr.class).forEach(call -> {
                                String command = call.getNameAsString();
                                if (DSL_KEYWORDS.contains(command)) {
                                    // 🚀 중요: 이제 extractTokens에 argContext를 같이 넘깁니다!
                                    List<String> rawArgs = extractTokens(call, segmentCu, argContext, methodMeta);
                                    processDslCommand(command, rawArgs, methodMeta, argContext);
                                }
                            });
                        });
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 추출된 DSL 명령어와 인자들을 분석하여 MethodMeta에 적절한 형태로 저장합니다.
     */
    private static void processDslCommand(String command, List<String> rawArgs, MethodMeta methodMeta, Map<String, String> argContext) {
        // 1. mapJoin 처리
        if ("mapJoin".equals(command)) {
            String raw = rawArgs.get(0);
            String fieldName = extractFieldNameFromMethodRef(raw);
            String alias = rawArgs.size() > 1 ? rawArgs.get(1) : null;

            // 어노테이션으로 매핑 타입 결정
            MapJoinMeta.MappingType mappingType = resolveMappingType(raw, fieldName);

            methodMeta.addMapJoin(new MapJoinMeta(fieldName, alias, mappingType));
            methodMeta.addStatement(new DslStatement(command, rawArgs));
        }
        // 2. JOIN 관련 명령어 처리 (중복 추가 방지!)
        else if (Arrays.asList("innerJoin", "leftJoin", "hashJoin", "mergeJoin", "loopJoin").contains(command)) {
            List<String> joinArgs = new ArrayList<>();

            // 0: Target Table (MEntity2.class)
            if (!rawArgs.isEmpty()) joinArgs.add(rawArgs.get(0));

            // 1: Left Column
            if (rawArgs.size() > 1) joinArgs.add(rawArgs.get(1));

            // 2: Right Column
            if (rawArgs.size() > 2) joinArgs.add(rawArgs.get(2));

            // 3. ✨ Alias 추출
            String extractedAlias = "";
            if (rawArgs.size() > 2 && rawArgs.get(2).contains("|")) {
                extractedAlias = rawArgs.get(2).split("\\|")[0]; // "u" 추출
            }

            // 기존 Binder와의 호환성을 위해 인자 리스트 구성 [Target, Left, Right, (Empty), Alias]
            while (joinArgs.size() < 4) joinArgs.add("");
            joinArgs.add(extractedAlias);

            methodMeta.addStatement(new DslStatement(command, joinArgs));
        }
        // 3. 그 외 일반 명령어 처리 (select, from, where 등)
        else {
            methodMeta.addStatement(new DslStatement(command, rawArgs));
            // 타겟 타입 추론 (from, mapTarget)
            if ("from".equals(command) && !rawArgs.isEmpty()) {
                String typeName = rawArgs.get(0).replace(".class", "");
                methodMeta.setTargetType(typeName);
            } else if ("mapTarget".equals(command) && !rawArgs.isEmpty()) {
                String dtoName = rawArgs.get(0).replace(".class", "");
                methodMeta.setTargetType(dtoName);
            }

        }
    }

    private static String extractSegmentTypeName(ClassOrInterfaceDeclaration classDecl) {
        return classDecl.getExtendedTypes().stream()
                // 1. 특정 부모 클래스를 상속받았는지 확인
                .filter(type -> type.getNameAsString().equals("JpmAbstractQuerySegment"))
                // 2. 제네릭 인자 <T>가 존재하는지 확인
                .filter(type -> type.getTypeArguments().isPresent())
                // 3. 첫 번째 인자 추출
                .map(type -> type.getTypeArguments().get().get(0).toString())
                .findFirst()
                .orElse(null);
    }



    private static List<String> extractTokens(MethodCallExpr call, CompilationUnit cu, Map<String, String> argContext, MethodMeta methodMeta) {
        List<String> args = new ArrayList<>();
        String command = call.getNameAsString();
        boolean isCondition = Arrays.asList("where", "and", "or").contains(command);



        String[] firstArgInfo = null;
        for (int i = 0; i < call.getArguments().size(); i++) {
            Expression arg = call.getArgument(i);
            // where/and/or의 세 번째 인자(index=2)만 quoteString=true
            boolean quoteString = isCondition && i == 2;
            if(isCondition && i == 0)
            {
                String column = resolveValue(call.getArgument(0), argContext, false);

                firstArgInfo = splitEntityAndField(column);
            }

            if (isCondition && i == 2 && firstArgInfo != null) {
                validateLiteralType(firstArgInfo, arg);
                firstArgInfo = null;
            }


            String resolved = resolveValue(arg, argContext, quoteString);

            if (resolved.contains("::") && !Arrays.asList("mapJoin", "mapResult").contains(command)) {
                resolved = extractFieldNameFromMethodRef(resolved);
            }
            args.add(resolved);
        }
        return args;
    }

    /**
     * 리터럴 혹은 변수를 상황에 맞게 문자열로 변환하는 헬퍼 메서드
     * (col 내부에 raw() 등이 중첩되어 있을 때도 대응하기 위함)
     */
    /**
     * 리터럴, 변수, 메서드 참조, 그리고 중첩된 col/as 호출을
     * 실제 SQL 문자열 조각으로 변환하는 통합 리졸버
     */


    private static String resolveValue(Expression expr, Map<String, String> argContext,  boolean quoteString) {
        if (expr.isMethodCallExpr()) {
            MethodCallExpr mCall = expr.asMethodCallExpr();
            String name = mCall.getNameAsString();


            // 1. raw: 알맹이만 그대로 반환
            if ("raw".equals(name)) {
                return resolveValue(mCall.getArgument(0), argContext, false);
            }

            // 2. quoted: 알맹이를 꺼내서 앞뒤에 ' 추가
            if ("quoted".equals(name)) {
                String inner = resolveValue(mCall.getArgument(0), argContext, false);
                // 만약 메서드 참조라면 필드명으로 먼저 변환
                if (inner.contains("::")) inner = extractFieldNameFromMethodRef(inner);
                return "'" + inner + "'";
            }

            // 3. bind: 알맹이를 꺼내서 #{} 추가
            if ("bind".equals(name)) {
                String inner = resolveValue(mCall.getArgument(0), argContext, false);
                if (inner.contains("::")) inner = extractFieldNameFromMethodRef(inner);
                return "#{" + inner + "}";
            }

            // 4. col/as (이전 로직 동일)
            if ("as".equals(name) && mCall.getScope().isPresent()) {
                return resolveValue(mCall.getScope().get(), argContext, false) + " AS " + resolveValue(mCall.getArgument(0), argContext, false);
            }
            if ("col".equals(name)) {
                String alias = resolveValue(mCall.getArgument(0), argContext, false);
                String field = resolveValue(mCall.getArgument(1), argContext, false);
                if (field.contains("::")) field = extractFieldNameFromMethodRef(field);

                // "item_summary.order_id" 형태로 반환
                return alias + "." + field;
            }




        }






        // boolean 리터럴 (true/false) -> TRUE/FALSE
        if (expr.isBooleanLiteralExpr()) {
            return expr.asBooleanLiteralExpr().getValue() ? "TRUE" : "FALSE";
        }

        if (expr.isStringLiteralExpr()) {
            String val = expr.asStringLiteralExpr().getValue();
            return quoteString ? "'" + val + "'" : val;
        }




        // 메서드 참조 (OrderEntity::getTotalPrice)
        if (expr.isMethodReferenceExpr()) {
            MethodReferenceExpr mRef = expr.asMethodReferenceExpr();
            return mRef.getScope().toString() + "::" + mRef.getIdentifier();
        }

        // 문자열 리터럴 ("totalPrice") -> 따옴표 없는 순수 값만!
        if (expr.isStringLiteralExpr()) {
            return expr.asStringLiteralExpr().getValue();
        }

        // 변수 (alias 등)
        if (expr.isNameExpr()) {
            String name = expr.asNameExpr().getNameAsString();

            // 🚀 개선: argContext에 'params'라는 키로 메서드 파라미터 목록을 넣어뒀다고 가정
            if (argContext != null && argContext.containsKey("isParam_" + name)) {
                return "#{" + name + "}";
            }

            // 🚀 개선: 테이블 별칭(Alias)으로 등록된 이름인지 확인
            if (argContext != null && argContext.containsKey("alias_" + name)) {
                return argContext.get("alias_" + name);
            }

            return name;
        }



        return expr.toString().replace("\"", "");
    }



    private static String[] splitEntityAndField(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;

        String cleaned = raw.trim();

        // alias|UserEntity::getOrders 형태 처리
        if (cleaned.contains("|")) {
            String[] pipeParts = cleaned.split("\\|");
            cleaned = pipeParts[pipeParts.length - 1];
        }

        // OrderEntity::getIsDeleted -> ["OrderEntity", "isDeleted"]
        if (cleaned.contains("::")) {
            String[] parts = cleaned.split("::");
            String className = parts[0].trim();
            String fieldName = convertGetterToField(parts[1].trim());
            System.out.println("fieldName: " + fieldName);

            return new String[]{className, fieldName};
        }

        // 단순 값 (숫자, TRUE/FALSE 등) -> ["STRING", raw값]
        throw new RuntimeException("함수형 값이 안들어왔습니다!!!");
    }

    private static String[] resolveLiteralType(String cleaned) {
        if (cleaned == null || cleaned.trim().isEmpty()) return null;
        cleaned = cleaned.trim();

        // BOOLEAN
        if (cleaned.equals("TRUE") || cleaned.equals("FALSE")) {
            return new String[]{"BOOLEAN", cleaned};
        }

        // LONG (100, 200L)
        if (cleaned.matches("-?\\d+[Ll]?")) {
            return new String[]{"LONG", cleaned};
        }

        // DOUBLE (3.14)
        if (cleaned.matches("-?\\d+\\.\\d+")) {
            return new String[]{"DOUBLE", cleaned};
        }


        return new String[]{"INTEGER", cleaned};
    }


    private static String extractFieldNameFromMethodRef(String raw) {

        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String cleaned = raw.trim();

        // 🔥 1️⃣ alias|UserEntity::getOrders 형태 처리
        if (cleaned.contains("|")) {
            String[] pipeParts = cleaned.split("\\|");
            cleaned = pipeParts[pipeParts.length - 1];
            // 마지막 파트가 실제 MethodRef
        }

        // 🔥 2️⃣ :: 기준으로 메서드명 추출
        System.out.println("cleaned: " + cleaned);




        return cleaned;
    }


 /*   private static String convertGetterToField(String className, String methodName) {

        EntityMeta entityMeta = EntityMetaRegistry.getEntityMeta(className);

        if (methodName.startsWith("get") && methodName.length() > 3) {
            String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);

            if(entityMeta == null) {
                return fieldName;
            }
            return entityMeta.getColumn(fieldName);
        }

        if (methodName.startsWith("is") && methodName.length() > 2) {
            String fieldName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);

            if(entityMeta == null) {
                return fieldName;
            }
            return entityMeta.getColumn(fieldName);

        }

        return methodName;
    }*/

    private static String convertGetterToField(String methodName) {




        System.out.println("convertGetterToField: " + methodName);


        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }

        if (methodName.startsWith("is") && methodName.length() > 2) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }

        return methodName;
    }


    /**
     * "MEntity1::getOrders" 같은 메서드 참조에서
     * 해당 필드가 List 타입인지 EntityMetaRegistry를 통해 확인합니다.
     */

    private static MapJoinMeta.MappingType resolveMappingType(String raw, String fieldName) {
        // "alias|ClassName::getField" 또는 "ClassName::getField" 에서 클래스명 추출
        String classNamePart = raw.contains("|")
                ? raw.split("\\|")[1].split("::")[0].trim()
                : raw.contains("::") ? raw.split("::")[0].trim() : null;

        if (classNamePart == null) return MapJoinMeta.MappingType.AUTO;

        try {
            // EntityMetaRegistry에서 실제 클래스 가져오기
            EntityMeta meta = entityMetaRegistry.getEntityMeta(classNamePart);
            if (meta == null) return MapJoinMeta.MappingType.AUTO;

            Class<?> entityClass = entityMetaRegistry.getEntityClass(classNamePart);
            Field field = entityClass.getDeclaredField(fieldName);

            if (field.isAnnotationPresent(MqCollection.class)) {
                return MapJoinMeta.MappingType.COLLECTION;
            }
            if (field.isAnnotationPresent(MqAssociation.class)) {
                return MapJoinMeta.MappingType.ASSOCIATION;
            }

            // 어노테이션 없으면 타입으로 자동 판별 (fallback)
            return List.class.isAssignableFrom(field.getType())
                    ? MapJoinMeta.MappingType.COLLECTION
                    : MapJoinMeta.MappingType.ASSOCIATION;

        } catch (Exception e) {
            return MapJoinMeta.MappingType.AUTO;
        }
    }




    //where and or 인자값 첫번째와 세번째 인자값 같은 타입인지 체크

    private static void validateLiteralType(String[] firstArgInfo, Expression lastArg) {


         String lastArgFieldType = null;
        if (lastArg.isStringLiteralExpr())       lastArgFieldType = "STRING";
        else if (lastArg.isBooleanLiteralExpr()) lastArgFieldType = "BOOLEAN";
        else if (lastArg.isIntegerLiteralExpr()) lastArgFieldType = "INTEGER";
        else if (lastArg.isLongLiteralExpr())    lastArgFieldType = "LONG";
        else if (lastArg.isDoubleLiteralExpr())  lastArgFieldType = "DOUBLE";
        else{ System.err.println("Invalid literal type: " + lastArg.getClass().getSimpleName()); return;}

        //무조건 MFieldType만 검증 가능
        String firstArgEntityName = firstArgInfo[0];


        String firstArgFieldName = firstArgInfo[1];

        System.out.println("firestArgFieldName : " + firstArgFieldName);


        EntityMeta entityMeta= entityMetaRegistry.getEntityMeta(firstArgEntityName);


        if(entityMeta == null) {System.err.println("Invalid entity name: " + firstArgEntityName); return;}

        String firstArgMFieldType = entityMeta.getFieldType(firstArgFieldName);
        System.out.println("firstArgMFieldType: " + firstArgMFieldType);
        String firstArgFieldType = "";

        switch (Objects.requireNonNull(firstArgMFieldType).toUpperCase()) {
            // 숫자 정수계열
            case "INTEGER":
                firstArgFieldType = "INTEGER"; break;
            case "LONG":
            case "FK":
                firstArgFieldType = "LONG"; break;

            // 숫자 실수계열
            case "FLOAT":
            case "DOUBLE":
                firstArgFieldType = "DOUBLE"; break;
            // boolean
            case "BOOLEAN":
                firstArgFieldType = "BOOLEAN"; break;
            // 문자열 계열 (날짜도 '' 로 받으니까 STRING)
            case "STRING": case "TEXT": case "JSON":
            case "UUID_V_7":
            case "LOCAL_DATE": case "LOCAL_DATE_TIME":
                firstArgFieldType = "STRING"; break;

        }


        if (firstArgFieldType.equals("LONG")) {
            if (lastArgFieldType.equals("INTEGER")) return;
        }

        if (!firstArgFieldType.equals(lastArgFieldType)) {
            System.err.println("[타입 오류] " + firstArgEntityName + "." + firstArgFieldName
                    + " (" + firstArgMFieldType + ") 에 " + lastArgFieldType + " 타입 값 사용: " + lastArg);
            throw new RuntimeException(
                    "[타입 오류] " + firstArgEntityName + "." + firstArgFieldName
                            + " 은 " + firstArgMFieldType + " 인데 " + lastArgFieldType + " 를 사용했습니다."
            );
        }


    }


    // 파서 내부의 표현식 처리기




}