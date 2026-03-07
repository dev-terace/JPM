package io.jpm.core.jpm_repository.processor;

import io.jpm.config.ast.BaseAstProcessor;
import io.jpm.core.jpm_repository.processor.handler.find_repo_meta_handler.FindRepoMetaHandler;
import io.jpm.core.jpm_repository.processor.handler.SaveJpmRepoSourceLocationHandler;
import io.jpm.core.jpm_repository.processor.handler.SaveMqInjectSegmentPathHandler;
import io.jpm.core.jpm_repository.processor.handler.WriteAndExecuteDMLHandler;
import io.jpm.core.jpm_repository.processor.handler.handlerContext.AstJpmRepoContext;

public class AstJpmRepositoryProcessorV2 extends BaseAstProcessor {


    @Override
    protected void onInit() {
        AstJpmRepoContext handlerContext = new AstJpmRepoContext();



        /*addHandler(new SaveMqInjectSegmentPathHandler(cache, globalRegistry, context), handlerContext);*/
        addHandler(new SaveJpmRepoSourceLocationHandler(cache, globalRegistry, context), handlerContext);
        addHandler(new FindRepoMetaHandler(cache, globalRegistry, context), handlerContext);
        addHandler(new WriteAndExecuteDMLHandler(cache, globalRegistry, context), handlerContext);


    }
}
