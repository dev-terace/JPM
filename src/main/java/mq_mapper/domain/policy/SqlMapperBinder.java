package mq_mapper.domain.policy;

import mq_mapper.domain.vo.DslStatement;
import mq_mapper.domain.vo.EntityMeta;
import mq_mapper.domain.vo.MethodMeta;

import java.util.List;

public interface SqlMapperBinder {
    String generateSqlFromStatements(List<DslStatement> statements, EntityMeta entityMeta);
    String generateSql(MethodMeta method, EntityMeta entityMeta);
}
