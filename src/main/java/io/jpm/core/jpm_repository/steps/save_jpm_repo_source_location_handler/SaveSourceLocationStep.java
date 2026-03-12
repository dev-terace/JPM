package io.jpm.core.jpm_repository.steps.save_jpm_repo_source_location_handler;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.jpm.api.JpmQuerySegment;
import io.jpm.api.JpmRepository;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.exception.config.JpmChainExtractorScanner;
import io.jpm.common.exception.config.JpmToolbox;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class SaveSourceLocationStep implements Step<JpmRepoContext> {

    @Override
    public void execute(JpmRepoContext context) throws Exception {
        RoundEnvironment roundEnv        = context.getRoundEnv();
        Trees trees                      = context.getContext().getTrees();
        JpmToolbox jpmToolbox            = context.getContext().getJpmToolbox();
        JpmChainExtractorScanner scanner = context.getJpmChainExtractorScanner();
        ErrorTracker errorTracker        = context.getErrorTracker();
        BuildTimeMetadataCache cache     = context.getCache();

        for (Element element : roundEnv.getElementsAnnotatedWith(JpmRepository.class)) {
            processElement(element, trees, jpmToolbox, scanner, errorTracker, cache);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(JpmQuerySegment.class)) {
            processElement(element, trees, jpmToolbox, scanner, errorTracker, cache);
        }
    }

    private void processElement(
            Element element,
            Trees trees,
            JpmToolbox jpmToolbox,
            JpmChainExtractorScanner scanner,
            ErrorTracker errorTracker,
            BuildTimeMetadataCache cache
    ) {
        TypeElement repoElement = (TypeElement) element;
        TreePath path           = trees.getPath(repoElement);

        jpmToolbox.setCurrentCut(path.getCompilationUnit());

        String fqcn = repoElement.getQualifiedName().toString();

        scanner.setClassName(fqcn);
        errorTracker.setClassName(fqcn);
        errorTracker.setTrees(trees);
        scanner.init(element, cache.getSourceLocationCache());
        scanner.scan(path, null);
    }
}