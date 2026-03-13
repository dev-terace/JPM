package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context;

import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatement;

import java.util.List;

public class AliasScanContext implements Context {

    private final List<DslStatement> statements;
    private final BuildContext       buildContext;
    private final RepoMetaRegistry   repoMetaRegistry;

    public AliasScanContext(List<DslStatement> statements,
                            BuildContext buildContext,
                            RepoMetaRegistry repoMetaRegistry) {
        this.statements       = statements;
        this.buildContext     = buildContext;
        this.repoMetaRegistry = repoMetaRegistry;
    }

    public List<DslStatement> getStatements()       { return statements; }
    public BuildContext        getBuildContext()      { return buildContext; }
    public RepoMetaRegistry    getRepoMetaRegistry() { return repoMetaRegistry; }
}