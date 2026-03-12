package io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_mapper_binder;

import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;

import java.util.List;

@Deprecated
public interface SqlMapperBinder {
    String generateSqlFromStatements(List<DslStatement> statements, EntityMeta entityMeta);
    String generateSql(MethodMeta method, EntityMeta entityMeta);
}
