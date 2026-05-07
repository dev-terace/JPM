package io.jpm.core.jpm_repository.runtime.context;

import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;

import java.util.List;

public class AliasScanContextV2 implements Context {

    private final List<DslStatementV2> statements;
    private final BuildContext       buildContext;
    private final RepoMetaRegistry   repoMetaRegistry;

    public AliasScanContextV2(List<DslStatementV2> statements,
                              BuildContext buildContext, RepoMetaRegistry repoMetaRegistry) {
        this.statements       = statements;
        this.buildContext     = buildContext;
        this.repoMetaRegistry = repoMetaRegistry;
    }

    public List<DslStatementV2> getStatements()       { return statements; }
    public BuildContext        getBuildContext()      { return buildContext; }

    public RepoMetaRegistry getRepoMetaRegistry() {
        return repoMetaRegistry;
    }
}