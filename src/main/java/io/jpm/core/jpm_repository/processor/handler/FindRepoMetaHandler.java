package io.jpm.core.jpm_repository.processor.handler;

import com.sun.source.tree.*;
import com.sun.source.util.Trees;
import io.jpm.api.JpmRepository;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.AstHandler;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.core.jpm_repository.parse.domain.vo.DSLKeywords;
import io.jpm.core.jpm_repository.parse.domain.vo.MethodMeta;
import io.jpm.core.jpm_repository.parse.domain.vo.RepoMeta;
import io.jpm.core.jpm_repository.parse.infra.MapParamRegistry;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_dsl_command_proc.AstDslCommandProc;
import io.jpm.core.jpm_repository.parse.infra.ast.AstExpressionTreeValueResolver;
import io.jpm.core.jpm_repository.parse.infra.ast.AstMethodTreeUtil;
import io.jpm.core.jpm_repository.parse.infra.ast.argument_token_extractor.AstArgumentTokenExtractorV2;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_dsl_command_proc.AstDslCommandProcV2;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_segment_inliner.AstSegmentInlinerV2;
import io.jpm.core.jpm_repository.processor.handler.handlerContext.AstJpmRepoContext;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FindRepoMetaHandler extends AstHandler<AstJpmRepoContext> {

    private final AstContext astContext;
    private static final Set<String> DSL_KEYWORDS = DSLKeywords.getDSLKeywords();

    private final RepoMetaRegistry repoMetaRegistry;
    private final AstArgumentTokenExtractorV2 tokenExtractor;
    private final AstDslCommandProcV2 commandProcessor;
    private final MapParamRegistry mapParamRegistry;
    private final AstSegmentInlinerV2 segmentInliner;
    private final ErrorTracker errorTracker;



    public FindRepoMetaHandler(BuildTimeMetadataCache cache, GlobalRegistry globalRegistry, AstContext astContext) {
        super(cache, globalRegistry, astContext);

        this.astContext = astContext;
        this.repoMetaRegistry = cache.getRepoMetaRegistry();
        AstExpressionTreeValueResolver valueResolver = new AstExpressionTreeValueResolver(repoMetaRegistry);
        this.tokenExtractor     = new AstArgumentTokenExtractorV2(valueResolver, cache);
        this.commandProcessor   = new AstDslCommandProcV2(cache);
        this.mapParamRegistry = new MapParamRegistry();
        this.segmentInliner = new AstSegmentInlinerV2(repoMetaRegistry, tokenExtractor, commandProcessor, DSL_KEYWORDS);
        this.errorTracker = cache.getErrorTracker();

    }








    @Override
    public void handle(RoundEnvironment roundEnv) throws Exception {
        List<RepoMeta> repoMetas = new ArrayList<>();


        for (Element element : roundEnv.getElementsAnnotatedWith(JpmRepository.class)) {
        errorTracker.setClassName(element.getSimpleName().toString())
                .setTrees(astContext.getTrees());



        TypeElement repoElement = (TypeElement) element;
        String className  = element.getSimpleName().toString();
        String namespace  = extractNamespace(repoElement, className);
        RepoMeta repoMeta = new RepoMeta(className, namespace);

        Trees trees = astContext.getTrees();

        ClassTree classTree = trees.getTree(repoElement);
        if (classTree == null) {
            repoMetas.add(repoMeta);

            continue;
        }

        for (Tree member : classTree.getMembers()) {

            if (member instanceof MethodTree) {
                MethodMeta methodMeta = parseMethod((MethodTree) member, className);
                if (!methodMeta.getStatements().isEmpty()) {
                    repoMeta.addMethod(methodMeta);
                    LogPrinter.info("[FindRepoMetaHandler]"+ repoMeta.toString());
                }
            }
        }

        repoMetas.add(repoMeta);
        }


        handlerContext.setRepoMetas(repoMetas);

    }

    @Override
    public void setHandlerContext(AstJpmRepoContext handlerContext) {
        this.handlerContext = handlerContext;
    }


    private String extractNamespace(TypeElement repoElement, String defaultName) {
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




    private MethodMeta parseMethod(MethodTree methodTree, String className) {
        MethodMeta methodMeta = new MethodMeta(methodTree.getName().toString());

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
                    parseChain(expr, className, methodMeta);
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
                            MethodMeta methodMeta) {

        for (MethodInvocationTree call : AstMethodTreeUtil.flattenChain(expr)) {
            String command   = AstMethodTreeUtil.getMethodName(call);
            errorTracker.setMethodName(methodMeta.getMethodName());
            String scopeName = AstMethodTreeUtil.getScopeName(call);




            if (DSL_KEYWORDS.contains(command)) {
                List<String> rawArgs = tokenExtractor.extract(call, mapParamRegistry);

                commandProcessor.process(command, rawArgs, methodMeta);

            } else if (isSegmentCall(scopeName)) {
                List<String> passedArgs = tokenExtractor.extract(call, mapParamRegistry);


                String segmentFqcn = repoMetaRegistry.getSegmentPath(className, scopeName);


                if (segmentFqcn != null) {
                    segmentInliner.inline(astContext, className, scopeName, command,
                            methodMeta, passedArgs);
                }
            }
        }
    }

    private static boolean isSegmentCall(String scopeName) {
        return scopeName != null && scopeName.contains("segment");
    }

}
