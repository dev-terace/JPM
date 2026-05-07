package io.jpm.core.jpm_repository.runtime.context;

import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.model.RepoMeta;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;

public class BuildMethodDataContextV2 implements Context {

    private final JpmRepoContext jpmRepoContext;
    private final RepoMeta       repoMeta;

    public BuildMethodDataContextV2(JpmRepoContext jpmRepoContext, RepoMeta repoMeta) {
        this.jpmRepoContext = jpmRepoContext;
        this.repoMeta       = repoMeta;
    }

    public JpmRepoContext getJpmRepoContext() { return jpmRepoContext; }
    public RepoMeta       getRepoMeta()       { return repoMeta; }
}