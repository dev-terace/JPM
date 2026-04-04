package io.jpm.core.jpm_repository.domain.model;

public class MapJoinMeta {
    public enum MappingType { ASSOCIATION, COLLECTION, AUTO }

    private final String parentField;
    private final String alias;
    private final MappingType mappingType;
    private final String javaType;
    private final String pkFieldName;
    private final String pkColName;

    public MapJoinMeta(String javaType, String parentField, String alias, MappingType mappingType, String pkFieldName, String pkColName) {
        this.parentField = parentField;
        this.alias = alias;
        this.mappingType = mappingType;
        this.javaType = javaType;
        this.pkFieldName = pkFieldName;
        this.pkColName = pkColName;
    }


    public String getParentField() { return parentField; }
    public String getAlias() { return alias; }
    public MappingType getMappingType() { return mappingType; }
    public boolean isList() { return mappingType == MappingType.COLLECTION; }

    public String getJavaType() {
        return javaType;
    }

    public String getPkFieldName() {
        return pkFieldName;
    }

    public String getPkColName() {
        return pkColName;
    }
}