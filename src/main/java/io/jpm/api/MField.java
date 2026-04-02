package io.jpm.api;

import io.jpm.api.m_field_type.MFieldType;
import io.jpm.api.m_field_type.MFieldTypeEnum;

import javax.annotation.Nullable;

public class MField<T extends MFieldType> {

    private final String name;
    private final boolean primaryKey;
    private final boolean autoIncrement;
    private final boolean nullable;
    private final String defaultValue;
    private final int length;
    private final String parentClassName;
    private final String onDelete;
    private final boolean index;
    private final boolean unique;

    private String type;

    private MField(Builder builder) {
        this.name = builder.name;
        this.primaryKey = builder.primaryKey;
        this.autoIncrement = builder.autoIncrement;
        this.nullable = builder.nullable;
        this.defaultValue = builder.defaultValue;
        this.length = builder.length;
        this.parentClassName = builder.parentClassName;
        this.onDelete = builder.onDelete;
        this.index = builder.index;
        this.unique = builder.unique;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MFieldTypeEnum getType() {
        return MFieldTypeEnum.valueOf(type);
    }

    public String getName() { return name; }
    public boolean isPrimaryKey() { return primaryKey; }
    public boolean isAutoIncrement() { return autoIncrement; }
    public boolean isNullable() { return nullable; }
    @Nullable public String getDefaultValue() { return defaultValue; }
    public int getLength() { return length; }
    @Nullable public String getParentClassName() { return parentClassName; }
    public String getOnDelete() { return onDelete; }
    public boolean isIndex() { return index; }
    public boolean isUnique() { return unique; }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private boolean primaryKey = false;
        private boolean autoIncrement = false;
        private boolean nullable = true;
        private String defaultValue = null;
        private int length = 255;
        private String parentClassName = null;
        private String onDelete = OnDeleteType.NO_ACTION.getSql();
        private boolean index = false;
        private boolean unique = false;

        public Builder name(String name) { this.name = name; return this; }
        public Builder primaryKey(boolean val) { this.primaryKey = val; return this; }
        public Builder autoIncrement(boolean val) { this.autoIncrement = val; return this; }
        public Builder nullable(boolean val) { this.nullable = val; return this; }
        public Builder defaultValue(String val) { this.defaultValue = val; return this; }
        public Builder length(int val) { this.length = val; return this; }
        public Builder index(boolean val) { this.index = val; return this; }
        public Builder unique(boolean val) { this.unique = val; return this; }

        public Builder parent(Class<?> clazz) { this.parentClassName = clazz.getSimpleName(); return this; }
        public Builder parent(String className) { this.parentClassName = className; return this; }
        public Builder onDelete(OnDeleteType type) { this.onDelete = type.getSql(); return this; }

        public <T extends MFieldType> MField<T> build() { return new MField<>(this); }
    }
}