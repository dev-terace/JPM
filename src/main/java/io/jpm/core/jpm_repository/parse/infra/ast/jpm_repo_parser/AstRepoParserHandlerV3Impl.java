package io.jpm.core.jpm_repository.parse.infra.ast.jpm_repo_parser;


import com.sun.source.tree.*;
import com.sun.source.util.Trees;

import io.jpm.common.exception.ErrorCollector;
import io.jpm.core.jpm_repository.parse.domain.vo.DSLKeywords;
import io.jpm.core.jpm_repository.parse.domain.vo.MethodMeta;
import io.jpm.core.jpm_repository.parse.domain.vo.RepoMeta;
import io.jpm.core.jpm_repository.parse.infra.MapParamRegistry;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_dsl_command_proc.AstDslCommandProc;
import io.jpm.core.jpm_repository.parse.infra.ast.AstExpressionTreeValueResolver;
import io.jpm.core.jpm_repository.parse.infra.ast.AstMethodTreeUtil;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_segment_inliner.AstSegmentInliner;
import io.jpm.core.jpm_repository.parse.infra.ast.argument_token_extractor.AstArgumentTokenExtractor;
import io.jpm.common.utils.LogPrinter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import java.util.*;




public class AstRepoParserHandlerV3Impl {
    private static final Set<String> DSL_KEYWORDS = DSLKeywords.getDSLKeywords();

    // -------------------------------------------------------------------------
    // 협력 객체들
    // -------------------------------------------------------------------------
    private final RepoMetaRegistry repoMetaRegistry;
    private final AstExpressionTreeValueResolver valueResolver;
    private final AstArgumentTokenExtractor tokenExtractor;
    private final AstDslCommandProc commandProcessor;
    private final AstSegmentInliner astSegmentInliner;

    // -------------------------------------------------------------------------
    // 생성자 (의존성 주입)
    // -------------------------------------------------------------------------
    public AstRepoParserHandlerV3Impl(RepoMetaRegistry repoMetaRegistry) {
        this.repoMetaRegistry = repoMetaRegistry;
        this.valueResolver      = new AstExpressionTreeValueResolver(repoMetaRegistry);
        this.tokenExtractor     = new AstArgumentTokenExtractor(valueResolver);
        this.commandProcessor   = new AstDslCommandProc(repoMetaRegistry);
        this.astSegmentInliner = new AstSegmentInliner(repoMetaRegistry, tokenExtractor,
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
        MapParamRegistry mapParamRegistry = new MapParamRegistry();




        // 파라미터 등록
        for (VariableTree param : methodTree.getParameters()) {
            String paramName = param.getName().toString();
            String paramType = param.getType().toString();
            methodMeta.addParameter(paramName, paramType);
            mapParamRegistry.registerParam(paramName, paramType);
        }

        // 바디 파싱
        BlockTree body = methodTree.getBody();
        if (body != null) {
            for (StatementTree stmt : body.getStatements()) {
                if (stmt instanceof ExpressionStatementTree) {
                    ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
                    parseChain(expr, className, methodMeta, mapParamRegistry, env, trees);
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
                            MethodMeta methodMeta, MapParamRegistry mapParamRegistry,
                            ProcessingEnvironment env, Trees trees) {

        for (MethodInvocationTree call : AstMethodTreeUtil.flattenChain(expr)) {
            String command   = AstMethodTreeUtil.getMethodName(call);
            ErrorCollector.setMethodName(methodMeta.getMethodName());
            String scopeName = AstMethodTreeUtil.getScopeName(call);




            if (DSL_KEYWORDS.contains(command)) {
                List<String> rawArgs = tokenExtractor.extract(call, mapParamRegistry, methodMeta);

                commandProcessor.process(command, rawArgs, methodMeta, mapParamRegistry);

            } else if (isSegmentCall(scopeName)) {
                List<String> passedArgs = tokenExtractor.extract(call, mapParamRegistry, methodMeta);


                String segmentFqcn = repoMetaRegistry.getSegmentPath(className, scopeName);


                if (segmentFqcn != null) {
/*                    astSegmentInliner.inline(, className, scopeName, command,
                            methodMeta, passedArgs);*/
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
