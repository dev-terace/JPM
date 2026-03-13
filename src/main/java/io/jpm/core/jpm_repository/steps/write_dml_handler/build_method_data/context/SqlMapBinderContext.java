package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context;

import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;

public class SqlMapBinderContext implements Context {
    private final MethodMeta method;
    private final EntityMeta entityMeta;
    private String finalSql;
    private final BuildContext buildContext;

    public SqlMapBinderContext(MethodMeta method, EntityMeta entityMeta) {
        this.method = method;
        this.entityMeta = entityMeta;
        this.buildContext = new BuildContext(entityMeta);

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

    public MethodMeta getMethod() {
        return method;
    }

    public EntityMeta getEntityMeta() {
        return entityMeta;
    }








}
