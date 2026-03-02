package io.jpm.core.m_entity.parse.domain.vo;

public class MEntityInfo {
    String tableName;
    String pkColumnName;


    public MEntityInfo(String t, String p) { tableName = t; pkColumnName = p; }

    public String getTableName() {
        return tableName;
    }

    public String getPkColumnName() {
        return pkColumnName;
    }
}
