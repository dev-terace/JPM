package io.jpm.core.jpm_repository.steps.write_dml_handler.context;

import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;

public class SqlMapBinderContext {
    private final MethodMeta method;
    private final EntityMeta entityMeta;
    private String finalSql;

    public SqlMapBinderContext(MethodMeta method, EntityMeta entityMeta) {
        this.method = method;
        this.entityMeta = entityMeta;

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
