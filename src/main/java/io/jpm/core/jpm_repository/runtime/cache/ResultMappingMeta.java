package io.jpm.core.jpm_repository.runtime.cache;




public class ResultMappingMeta {
    private final String fieldName;
    private final String columnName;
    private final Class<?> javaType;
    private final boolean isId;


    private final String columnPrefix;


    public ResultMappingMeta(String fieldName, String columnName, Class<?> javaType, boolean isId) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.javaType = javaType;
        this.isId = isId;
        columnPrefix = null;
    }

    public ResultMappingMeta(String fieldName, String columnName, Class<?> javaType, boolean isId, String columnPrefix) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.javaType = javaType;
        this.isId = isId;
        this.columnPrefix = columnPrefix;
    }

    @Override
    public String toString() {
        return "ResultMappingMeta{" +
                "fieldName='" + fieldName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", javaType=" + (javaType != null ? javaType.getSimpleName() : "null") +
                ", isId=" + isId +
                ", columnPrefix='" + columnPrefix + '\'' +
                '}';
    }



}
