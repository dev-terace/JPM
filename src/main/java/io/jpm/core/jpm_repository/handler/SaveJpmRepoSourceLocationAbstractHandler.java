package io.jpm.core.jpm_repository.handler;

import io.jpm.config.ast.*;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;
import io.jpm.core.jpm_repository.steps.save_jpm_repo_source_location_handler.SaveSourceLocationStep;


public class SaveJpmRepoSourceLocationAbstractHandler implements AbstractHandler<JpmRepoContext> {

    private final Step<JpmRepoContext> saveSourceLocationStep = new SaveSourceLocationStep();




    /*public void handle(RoundEnvironment roundEnv) throws Exception {
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
