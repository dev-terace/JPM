package io.jpm.core.jpm_repository.steps.find_repo_meta_handler;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import io.jpm.api.JpmRepository;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.domain.model.RepoMeta;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.MethodParseContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.composite.CompositeParseMemberMethodStep;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FindRepoMetaStep implements Step<JpmRepoContext> {

    private final CompositeParseMemberMethodStep compositeParseMemberMethodStep;

    public FindRepoMetaStep(GlobalRegistry globalRegistry, AstContext astContext) {
        this.compositeParseMemberMethodStep = new CompositeParseMemberMethodStep(globalRegistry, astContext);
    }

    @Override
    public void execute(JpmRepoContext context) throws Exception {
        RoundEnvironment roundEnv = context.getRoundEnv();
        Trees trees               = context.getContext().getTrees();


        context.getContext().registerElements(roundEnv);

        List<RepoMeta> repoMetas = new ArrayList<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(JpmRepository.class)) {
            TypeElement repoElement = (TypeElement) element;
            ClassTree classTree     = trees.getTree(repoElement); // 여기서 가져오기
            RepoMeta repoMeta       = buildRepoMeta(element, context);

            for (Tree member : classTree.getMembers()) {
                if (member instanceof MethodTree) {
                    MethodMeta methodMeta     = new MethodMeta(((MethodTree) member).getName().toString());
                    MethodParseContext subCtx = new MethodParseContext(repoElement, (MethodTree) member, methodMeta);
                    compositeParseMemberMethodStep.execute(subCtx);

                    if (!methodMeta.getStatements().isEmpty()) {
                        LogPrinter.info("[FindRepoMetaStep] methodMEta: ]" + methodMeta);
                        repoMeta.addMethod(methodMeta);
                    }
                }
            }


            repoMetas.add(repoMeta);
        }



        context.setRepoMetas(repoMetas);
    }


    private RepoMeta buildRepoMeta(Element element, JpmRepoContext context) {
        ErrorTracker errorTracker = context.getErrorTracker();
        errorTracker.setClassName(element.getSimpleName().toString())
                .setTrees(context.getContext().getTrees());

        TypeElement repoElement = (TypeElement) element;
        String className        = element.getSimpleName().toString();
        String namespace        = extractNamespace(repoElement, className);

        return new RepoMeta(className, namespace);
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

}

