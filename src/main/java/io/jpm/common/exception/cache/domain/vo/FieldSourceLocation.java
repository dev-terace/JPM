package io.jpm.common.exception.cache.domain.vo;

import javax.lang.model.element.Element;

public class FieldSourceLocation extends SourceLocation {
    private final String entityName;
    private final String fieldName;

    private FieldSourceLocation(Builder builder) {
        super(builder.className, builder.lineNumber, builder.element);
        this.entityName = builder.entityName;
        this.fieldName  = builder.fieldName;
    }

    public String getEntityName() { return entityName; }
    public String getFieldName()  { return fieldName; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String className;
        private long lineNumber;
        private Element element;
        private String entityName;
        private String fieldName;

        public Builder className(String className)      { this.className = className;   return this; } // 버그 수정됨
        public Builder lineNumber(long lineNumber)      { this.lineNumber = lineNumber; return this; }
        public Builder element(Element element)         { this.element = element;       return this; }
        public Builder entityName(String entityName)    { this.entityName = entityName; return this; }
        public Builder fieldName(String fieldName)      { this.fieldName = fieldName;   return this; }

        public FieldSourceLocation build() { return new FieldSourceLocation(this); }
    }
}