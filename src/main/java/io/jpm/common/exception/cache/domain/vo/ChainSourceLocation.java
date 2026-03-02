package io.jpm.common.exception.cache.domain.vo;

import javax.lang.model.element.Element;

public class ChainSourceLocation extends SourceLocation {
    private final String methodName;
    private final String chainMethodName; // select, from, innerJoin, where ...
    private final int chainIndex;         // 몇 번째 체인인지
    private final String expression;      // ✅ 추가: 실제 소스 코드 (예: innerJoin(OrderEntity::getId))

    private ChainSourceLocation(Builder builder) {
        super(builder.className, builder.lineNumber, builder.element);
        this.methodName = builder.methodName;
        this.chainMethodName = builder.chainMethodName;
        this.chainIndex = builder.chainIndex;
        this.expression = builder.expression; // ✅ 필드 초기화
    }

    public String getMethodName() { return methodName; }
    public String getChainMethodName() { return chainMethodName; }
    public int getChainIndex() { return chainIndex; }
    public String getExpression() { return expression; } // ✅ Getter 추가

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String className;
        private long lineNumber;
        private Element element;
        private String methodName;
        private String chainMethodName;
        private int chainIndex;
        private String expression; // ✅ 빌더 필드 추가

        public Builder className(String className)               { this.className = className;             return this; }
        public Builder lineNumber(long lineNumber)               { this.lineNumber = lineNumber;           return this; }
        public Builder element(Element element)                  { this.element = element;                 return this; }
        public Builder methodName(String methodName)             { this.methodName = methodName;           return this; }
        public Builder chainMethodName(String chainMethodName)   { this.chainMethodName = chainMethodName; return this; }
        public Builder chainIndex(int chainIndex)                { this.chainIndex = chainIndex;           return this; }

        // ✅ Expression 세팅 메서드 추가
        public Builder expression(String expression)             { this.expression = expression;           return this; }

        public ChainSourceLocation build() { return new ChainSourceLocation(this); }
    }
}