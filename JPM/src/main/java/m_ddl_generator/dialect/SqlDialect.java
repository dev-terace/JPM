package m_ddl_generator.dialect;

import m_ddl_generator.model.ColumnMetadata;
import m_ddl_generator.model.TableMetadata;
import java.util.List;

public interface SqlDialect {
    List<String> createDropTableSql(TableMetadata table);
    String createTableDefinitionSql(TableMetadata table);
    List<String> createAlterTableSql(TableMetadata table, ColumnMetadata column); // FK 등 추가용
}