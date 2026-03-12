package io.jpm.core.jpm_repository.steps.write_dml_handler.composite;

import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;

import java.util.List;

public interface SqlMapBinder {
        String generateSqlFromStatements(List<DslStatement> statements, EntityMeta entityMeta);
}
