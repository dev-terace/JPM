package io.jpm.core.jpm_repository.runtime.context;

import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.model.*;

import java.util.List;

public class SqlMapBinderContextV2 implements Context {
    private final List<DslStatementV2> statements;
    private String finalSql;
    private final BuildContext buildContext;

    public SqlMapBinderContextV2(List<DslStatementV2> statements) {
        this.statements = statements;
        this.buildContext = new BuildContext();

    }




    public BuildContext getBuildContext() {
        return buildContext;
    }

    public String getFinalSql() {
        return finalSql;
    }

    public void setFinalSql(String finalSql) {
        this.finalSql = finalSql;
    }

    public List<DslStatementV2> getStatements() {
        return statements;
    }
}
