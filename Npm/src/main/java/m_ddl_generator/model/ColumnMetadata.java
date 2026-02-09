package m_ddl_generator.model;

public class ColumnMetadata {
    private final String name;
    private final String dbType;
    private final boolean isPrimaryKey;
    private final boolean isAutoIncrement;
    private final boolean isNullable;
    private final String defaultValue;
    private String fkTargetTable; // FK 없으면 null
    private String fkTargetColumn;
    private String onDeleteAction;

    // Builder 패턴이나 생성자로 초기화
    public ColumnMetadata(String name, String dbType, boolean isPrimaryKey, boolean isAutoIncrement,
                          boolean isNullable, String defaultValue) {
        this.name = name;
        this.dbType = dbType;
        this.isPrimaryKey = isPrimaryKey;
        this.isAutoIncrement = isAutoIncrement;
        this.isNullable = isNullable;
        this.defaultValue = defaultValue;
    }

    // FK 설정용 Setter
    public void setForeignKey(String targetTable, String targetColumn, String onDeleteAction) {
        this.fkTargetTable = targetTable;
        this.fkTargetColumn = targetColumn;
        this.onDeleteAction = onDeleteAction;
    }

    // Getters...
    public String getName() { return name; }
    public String getDbType() { return dbType; }
    public boolean isPrimaryKey() { return isPrimaryKey; }
    public boolean isAutoIncrement() { return isAutoIncrement; }
    public boolean isNullable() { return isNullable; }
    public String getDefaultValue() { return defaultValue; }
    public boolean isForeignKey() { return fkTargetTable != null; }
    public String getFkTargetTable() { return fkTargetTable; }
    public String getFkTargetColumn() { return fkTargetColumn; }
    public String getOnDeleteAction() { return onDeleteAction; }
}