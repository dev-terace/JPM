package io.jpm.core.jpm_repository.handler;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.jpm.api.JpmQuerySegment;
import io.jpm.api.JpmRepository;
import io.jpm.common.exception.config.JpmToolbox;
import io.jpm.config.ast.*;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;
import io.jpm.core.jpm_repository.steps.save_jpm_repo_source_location_handler.SaveSourceLocationMainStep;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;


public class SaveJpmRepoSourceLocationAbstractHandler implements AbstractHandler<JpmRepoContext> {

    private final Step<JpmRepoContext> saveSourceLocationStep = new SaveSourceLocationMainStep();



/*
    public void handle(RoundEnvironment roundEnv) throws Exception {
        // 기존 JpmRepository 처리
        for (Element element : roundEnv.getElementsAnnotatedWith(JpmRepository.class)) {
            processElement(element);
        }

        // JpmQuerySegment 추가
        for (Element element : roundEnv.getElementsAnnotatedWith(JpmQuerySegment.class)) {
            processElement(element);
        }
    }

    private void processElement(Element element) {
        TypeElement repoElement = (TypeElement) element;
        Trees tree = context.getTrees();
        JpmToolbox jpmToolbox = context.getJpmToolbox();

        TreePath path = tree.getPath(repoElement);
        jpmToolbox.setCurrentCut(path.getCompilationUnit());

        String fqcn = repoElement.getQualifiedName().toString(); // FQN

        jpmChainExtractorScanner.setClassName(fqcn);
        errorTracker.setClassName(fqcn);
        errorTracker.setTrees(tree);
        jpmChainExtractorScanner.init(element, cache.getSourceLocationCache());
        jpmChainExtractorScanner.scan(path, null);
    }*/



    @Override
    public void handle(JpmRepoContext context) throws Exception {
        saveSourceLocationStep.execute(context);

    }
}
