package io.jpm.core.jpm_repository.parse.domain.policy.sql_mapper_binder;

import io.jpm.core.jpm_repository.parse.domain.vo.DslStatement;
import io.jpm.core.jpm_repository.parse.domain.vo.EntityMeta;
import io.jpm.core.jpm_repository.parse.domain.vo.MethodMeta;

import java.util.List;

public interface SqlMapperBinder {
    String generateSqlFromStatements(List<DslStatement> statements, EntityMeta entityMeta);
    String generateSql(MethodMeta method, EntityMeta entityMeta);
}
