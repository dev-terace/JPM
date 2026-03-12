package io.jpm.core.jpm_repository.steps.write_dml_handler.context;

import io.jpm.core.jpm_repository.domain.model.RepoMeta;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;

public class BuildMethodDataContext {

    private final JpmRepoContext jpmRepoContext;
    private final RepoMeta       repoMeta;

    public BuildMethodDataContext(JpmRepoContext jpmRepoContext, RepoMeta repoMeta) {
        this.jpmRepoContext = jpmRepoContext;
        this.repoMeta       = repoMeta;
    }

    public JpmRepoContext getJpmRepoContext() { return jpmRepoContext; }
    public RepoMeta       getRepoMeta()       { return repoMeta; }
}