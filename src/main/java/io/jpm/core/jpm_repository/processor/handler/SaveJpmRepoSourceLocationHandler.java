package io.jpm.core.jpm_repository.processor.handler;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.jpm.api.JpmRepository;
import io.jpm.common.exception.ErrorCollector;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.exception.config.JpmChainExtractorScanner;
import io.jpm.common.exception.config.JpmToolbox;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.AstHandler;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.core.jpm_repository.processor.handler.handlerContext.AstJpmRepoContext;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;


public class SaveJpmRepoSourceLocationHandler extends AstHandler<AstJpmRepoContext> {

    private final AstContext context;
    private final JpmChainExtractorScanner jpmChainExtractorScanner;
    private final ErrorTracker errorTracker;
    public SaveJpmRepoSourceLocationHandler(BuildTimeMetadataCache cache, GlobalRegistry globalRegistry, AstContext astContext) {
        super(cache, globalRegistry, astContext);
        this.context = astContext;
        this.jpmChainExtractorScanner = new JpmChainExtractorScanner(astContext.getJpmToolbox());
        this.errorTracker = cache.getErrorTracker();
    }






    @Override
    public void handle(RoundEnvironment roundEnv) throws Exception {
        for (Element element : roundEnv.getElementsAnnotatedWith(JpmRepository.class)) {

            TypeElement repoElement = (TypeElement) element;
            Trees tree = context.getTrees();

            JpmToolbox jpmToolbox = context.getJpmToolbox();

            TreePath path = tree.getPath(repoElement);
            jpmToolbox.setCurrentCut(path.getCompilationUnit());
            jpmChainExtractorScanner.setClassName(element.getSimpleName().toString());
            errorTracker.setClassName(repoElement.getSimpleName().toString());
            errorTracker.setTrees(tree);
            jpmChainExtractorScanner.init(element, cache.getSourceLocationCache());
            jpmChainExtractorScanner.scan(path, null);
        }
    }

    @Override
    public void setHandlerContext(AstJpmRepoContext handlerContext) {
        this.handlerContext = handlerContext;
    }



}
