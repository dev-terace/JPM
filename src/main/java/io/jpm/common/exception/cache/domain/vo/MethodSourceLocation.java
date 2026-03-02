package io.jpm.common.exception.cache.domain.vo;

import javax.lang.model.element.Element;

public class MethodSourceLocation extends SourceLocation {
    private final String methodName;      // findOrderWithItemsMapped
    private final AnnotationType type;    // REPOSITORY or SEGMENT (의존성 수정됨)

    private MethodSourceLocation(Builder builder) {
        super(builder.className, builder.lineNumber, builder.element);
        this.methodName = builder.methodName;
        this.type = builder.type;
    }

    public String getMethodName() { return methodName; }
    public AnnotationType getType() { return type; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String className;
        private long lineNumber;
        private Element element;
        private String methodName;
        private AnnotationType type;

        public Builder className(String className)    { this.className = className;   return this; }
        public Builder lineNumber(long lineNumber)    { this.lineNumber = lineNumber; return this; }
        public Builder element(Element element)       { this.element = element;       return this; }
        public Builder methodName(String methodName)  { this.methodName = methodName; return this; }
        public Builder type(AnnotationType type)      { this.type = type;             return this; }

        public MethodSourceLocation build() { return new MethodSourceLocation(this); }
    }
}