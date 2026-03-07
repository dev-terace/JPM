package io.jpm.core.jpm_repository.processor.handler.find_repo_meta_handler;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
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
import io.jpm.core.jpm_repository.parse.infra.ast.AstExpressionTreeValueResolver;
import io.jpm.core.jpm_repository.parse.infra.ast.AstMethodTreeUtil;
import io.jpm.core.jpm_repository.parse.infra.ast.argument_token_extractor.AstArgumentTokenExtractorV2;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_dsl_command_proc.AstDslCommandProcV2;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_segment_inliner.AstSegmentInlinerV3;
import io.jpm.core.jpm_repository.processor.handler.handlerContext.AstJpmRepoContext;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


//내일 할거
//현재 segment가 scopeName 기준으로 cache에서 repoClass, scopeName 식으로 segment 타겟 클래스를 찾음
//segmentPath cache를 삭제하고 segmentInliner에서 arg targetClass와 메서드를 읽어야함 


//app config 로 전부 globalRegistry로 넘김
public class FindRepoMetaHandler extends AstHandler<AstJpmRepoContext> {


    private static final Set<String> DSL_KEYWORDS = DSLKeywords.getDSLKeywords();


    private final ErrorTracker errorTracker;
    private final Trees trees;
    private final FindRepoMetaHandlerProc findRepoMetaHandlerProc;
    private final AstContext astContext;

    public FindRepoMetaHandler(BuildTimeMetadataCache cache, GlobalRegistry globalRegistry, AstContext astContext) {
        super(cache, globalRegistry, astContext);

        this.astContext = astContext;
        this.errorTracker = globalRegistry.errorTracker();
        this.trees = astContext.getTrees();

        this.findRepoMetaHandlerProc = new FindRepoMetaHandlerProc(globalRegistry, astContext);
    }








    @Override
    public void handle(RoundEnvironment roundEnv) throws Exception {


        List<RepoMeta> repoMetas = new ArrayList<>();

        context.registerElements(roundEnv);

        for (Element element : roundEnv.getElementsAnnotatedWith(JpmRepository.class)) {

            TypeElement repoElement = (TypeElement) element;
            ClassTree classTree = trees.getTree(repoElement);
            RepoMeta repoMeta = buildRepoMeta(element);

            for (Tree member : classTree.getMembers()) {
                if (member instanceof MethodTree) {
                    findRepoMetaHandlerProc.parseMemberMethod((MethodTree) member, repoElement, repoMeta);
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



    private RepoMeta buildRepoMeta(Element element) {
        errorTracker.setClassName(element.getSimpleName().toString())
                .setTrees(astContext.getTrees());

        TypeElement repoElement = (TypeElement) element;
        String className = element.getSimpleName().toString();
        String namespace = buildRepoMetaExtractNamespace(repoElement, className);


        return new RepoMeta(className, namespace);
    }


    private String buildRepoMetaExtractNamespace(TypeElement repoElement, String defaultName) {
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
