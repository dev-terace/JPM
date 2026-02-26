package m_entity.generator.domain.vo;

import java.util.List;

public class DDLTableMetadata {
    private final String tableName;
    private final List<DDLColumnMetadata> columns;

    public DDLTableMetadata(String tableName, List<DDLColumnMetadata> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }
    // Getters...
    public String getTableName() { return tableName; }
    public List<DDLColumnMetadata> getColumns() { return columns; }


}