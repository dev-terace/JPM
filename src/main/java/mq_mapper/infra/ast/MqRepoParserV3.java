package mq_mapper.infra.ast;


import com.sun.source.tree.*;
import com.sun.source.util.Trees;

import mq_mapper.domain.vo.MethodMeta;
import mq_mapper.domain.vo.RepoMeta;
import mq_mapper.infra.ast.utils.MethodTreeUtil;
import mq_mapper.infra.repo.EntityMetaRegistry;
import utils.LogPrinter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.*;




public class MqRepoParserV3 {
    private static final Set<String> DSL_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "select", "from", "where", "and", "or", "andGroup", "orGroup", "endGroup",
            "innerJoin", "leftJoin", "hashJoin", "mergeJoin", "loopJoin",
            "insertInto", "update", "deleteFrom", "value", "set", "setRaw",
            "orderBy", "groupBy", "limit", "offset", "sql", "selectRaw", "orderByRaw", "groupByRaw",
            "whereInGroup", "group", "fromGroup", "selectCase",
            "mapTarget", "mapId", "mapResult", "mapJoin", "innerJoinGroup", "leftJoinGroup",
            "whereExistsGroup", "whereNotExistsGroup"
    )));

    // -------------------------------------------------------------------------
    // 협력 객체들
    // -------------------------------------------------------------------------
    private final EntityMetaRegistry       entityMetaRegistry;
    private final ExpressionTreeValueResolver valueResolver;
    private final ArgumentTokenExtractor   tokenExtractor;
    private final DslCommandProcessor      commandProcessor;
    private final SegmentInliner           segmentInliner;

    // -------------------------------------------------------------------------
    // 생성자 (의존성 주입)
    // -------------------------------------------------------------------------
    public MqRepoParserV3(EntityMetaRegistry entityMetaRegistry) {
        this.entityMetaRegistry = entityMetaRegistry;
        this.valueResolver      = new ExpressionTreeValueResolver(entityMetaRegistry);
        this.tokenExtractor     = new ArgumentTokenExtractor(valueResolver);
        this.commandProcessor   = new DslCommandProcessor(entityMetaRegistry);
        this.segmentInliner     = new SegmentInliner(entityMetaRegistry, tokenExtractor,
                commandProcessor, DSL_KEYWORDS);
    }





    // -------------------------------------------------------------------------
    // 공개 API
    // -------------------------------------------------------------------------

    /**
     * 어노테이션 프로세서에서 찾은 TypeElement 를 분석해 {@link RepoMeta} 를 반환합니다.
     */
    public RepoMeta parseRepo(TypeElement repoElement, ProcessingEnvironment env, Trees trees) {
        String className  = repoElement.getSimpleName().toString();
        String namespace  = extractNamespace(repoElement, className);
        RepoMeta repoMeta = new RepoMeta(className, namespace);

        ClassTree classTree = trees.getTree(repoElement);
        if (classTree == null) return repoMeta;

        for (Tree member : classTree.getMembers()) {
            if (member instanceof MethodTree) {
                MethodMeta methodMeta = parseMethod((MethodTree) member, className, env, trees);
                if (!methodMeta.getStatements().isEmpty()) {
                    repoMeta.addMethod(methodMeta);
                }
            }
        }
        return repoMeta;
    }

    // -------------------------------------------------------------------------
    // private – 메서드 단위 파싱
    // -------------------------------------------------------------------------

    private MethodMeta parseMethod(MethodTree methodTree, String className,
                                   ProcessingEnvironment env, Trees trees) {
        MethodMeta methodMeta = new MethodMeta(methodTree.getName().toString());
        ArgContext argContext  = new ArgContext();

        // 파라미터 등록
        for (VariableTree param : methodTree.getParameters()) {
            String paramName = param.getName().toString();
            String paramType = param.getType().toString();
            methodMeta.addParameter(paramName, paramType);
            argContext.registerParam(paramName, paramType);
        }

        // 바디 파싱
        BlockTree body = methodTree.getBody();
        if (body != null) {
            for (StatementTree stmt : body.getStatements()) {
                if (stmt instanceof ExpressionStatementTree) {
                    ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
                    parseChain(expr, className, methodMeta, argContext, env, trees);
                }
            }
        }

        LogPrinter.info("[METHOD] name=" + methodTree.getName()
                + " statements=" + methodMeta.getStatements().size()
                + " params=" + methodMeta.getParameters().size());
        return methodMeta;
    }

    /** 체이닝된 DSL 호출을 순서대로 처리합니다. */
    private void parseChain(ExpressionTree expr, String className,
                            MethodMeta methodMeta, ArgContext argContext,
                            ProcessingEnvironment env, Trees trees) {

        for (MethodInvocationTree call : MethodTreeUtil.flattenChain(expr)) {
            String command   = MethodTreeUtil.getMethodName(call);
            String scopeName = MethodTreeUtil.getScopeName(call);

            LogPrinter.info("call: " + call.getMethodSelect());

            if (DSL_KEYWORDS.contains(command)) {
                List<String> rawArgs = tokenExtractor.extract(call, argContext, methodMeta);
                commandProcessor.process(command, rawArgs, methodMeta, argContext);

            } else if (isSegmentCall(scopeName)) {
                List<String> passedArgs = tokenExtractor.extract(call, argContext, methodMeta);


                String segmentFqcn = entityMetaRegistry.getSegmentPath(className, scopeName);


                if (segmentFqcn != null) {
                    segmentInliner.inline(env, trees, className, scopeName, command,
                            methodMeta, passedArgs);
                }
            }
        }
    }

    private static boolean isSegmentCall(String scopeName) {
        return scopeName != null && scopeName.contains("segment");
    }

    // -------------------------------------------------------------------------
    // private – 네임스페이스 추출
    // -------------------------------------------------------------------------

    private static String extractNamespace(TypeElement repoElement, String defaultName) {
        for (AnnotationMirror mirror : repoElement.getAnnotationMirrors()) {
            String annoName = mirror.getAnnotationType().asElement().getSimpleName().toString();
            if ("JpmRepository".equals(annoName) || "MqRepository".equals(annoName)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                        : mirror.getElementValues().entrySet()) {
                    if ("name".equals(entry.getKey().getSimpleName().toString())) {
                        String value = entry.getValue().getValue().toString();
                        if (!value.trim().isEmpty()) return value;
                    }
                }
            }
        }
        return defaultName;
    }
}
