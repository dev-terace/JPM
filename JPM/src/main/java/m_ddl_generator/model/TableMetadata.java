package m_ddl_generator.model;

import java.util.List;

public class TableMetadata {
    private final String tableName;
    private final List<ColumnMetadata> columns;

    public TableMetadata(String tableName, List<ColumnMetadata> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }
    // Getters...
    public String getTableName() { return tableName; }
    public List<ColumnMetadata> getColumns() { return columns; }
}