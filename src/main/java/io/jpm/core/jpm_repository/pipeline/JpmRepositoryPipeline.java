package io.jpm.core.jpm_repository.pipeline;

import io.jpm.config.ast.AbstractPipeline;
import io.jpm.config.ast.BasePipeline;
import io.jpm.core.jpm_repository.handler.find_repo_meta_handler.FindRepoMetaHandler;
import io.jpm.core.jpm_repository.handler.SaveJpmRepoSourceLocationAbstractHandler;
import io.jpm.core.jpm_repository.handler.WriteDMLHandler;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;

public class JpmRepositoryPipeline extends AbstractPipeline {


    @Override
    protected void onInit() {
        JpmRepoContext handlerContext = new JpmRepoContext(roundEnv, cache, context, globalRegistry);


        addHandler(new SaveJpmRepoSourceLocationAbstractHandler(), handlerContext);
        addHandler(new FindRepoMetaHandler(), handlerContext);
        addHandler(new WriteDMLHandler(), handlerContext);


    }
}
