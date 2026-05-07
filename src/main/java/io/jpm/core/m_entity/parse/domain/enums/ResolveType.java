package io.jpm.core.m_entity.parse.domain.enums;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public enum ResolveType {

    INTEGER("INT", Integer.class),
    LONG("BIGINT", Long.class),
    FK("BIGINT", Long.class),
    FLOAT("FLOAT", Float.class),
    DOUBLE("DOUBLE PRECISION", Double.class),
    BOOLEAN("BOOLEAN", Boolean.class),
    LOCAL_DATE("DATE", LocalDate.class),
    LOCAL_DATE_TIME("TIMESTAMP", LocalDateTime.class),
    TEXT("TEXT", String.class),
    STRING("VARCHAR(255)", String.class),
    UUID_V_7("UUID", UUID.class);

    private final String sqlType;
    private final Class<?> javaType;

    ResolveType(String sqlType, Class<?> javaType) {
        this.sqlType = sqlType;
        this.javaType = javaType;
    }

    public String getSqlType() {
        return sqlType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }
}