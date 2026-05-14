package io.jpm.core.jpm_repository.runtime.context;

import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;
import io.jpm.core.jpm_repository.runtime.cache.ResultMappingMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddResultMapMetaContext implements Context {

    private final List<DslStatementV2> statements;
    private final Map<String, String> tableAliases;
    private final List<ResultMappingMeta> resultMappings;
    private final BuildContext buildContext;

    public AddResultMapMetaContext(List<DslStatementV2> statements, Map<String, String> tableAliases, BuildContext buildContext) {
        this.statements = statements;
        this.tableAliases = tableAliases;
        this.buildContext = buildContext;
        this.resultMappings = new ArrayList<>();
    }

    public List<DslStatementV2> getStatements() {
        return statements;
    }

    public Map<String, String> getTableAliases() {
        return tableAliases;
    }

    public List<ResultMappingMeta> getResultMappings() {
        return resultMappings;
    }

    public BuildContext getBuildContext() {
        return buildContext;
    }
}
