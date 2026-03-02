package io.jpm.common.exception;

import javax.lang.model.element.Element;

public class ErrorInfo {

    private final ErrorCode err;
    private final String className;

    private final String args;
    private final String methodName;
    private final String chainMethodName;
    // ✅ Element 필드 추가
    private final Element errorElement;

    private ErrorInfo(Builder builder) {
        this.err = builder.err;
        this.className = builder.className;

        this.args = builder.args;
        this.methodName = builder.methodName;
        this.chainMethodName = builder.chainMethodName;
        this.errorElement = builder.errorElement;
    }

    public ErrorCode getErr() { return err; }
    public String getClassName() { return className; }

    public String getArgs() { return args; }
    public String getMethodName() { return methodName; }
    public String getChainMethodName() { return chainMethodName; }
    // ✅ Element Getter 추가
    public Element getErrorElement() { return errorElement; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ErrorCode err;
        private String className;
        private String args;
        private String methodName;
        private String chainMethodName;
        private Element errorElement; // ✅ 빌더 내부 필드 추가

        public Builder err(ErrorCode err) {
            this.err = err;
            return this;
        }

        public Builder className(String className) {
            this.className = className;
            return this;
        }



        public Builder expression(String args) {
            this.args = args;
            return this;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder chainMethodName(String chainMethodName) {
            this.chainMethodName = chainMethodName;
            return this;
        }

        // ✅ Element 세팅 메서드 추가
        public Builder errorElement(Element errorElement) {
            this.errorElement = errorElement;
            return this;
        }

        public ErrorInfo build() {
            return new ErrorInfo(this);
        }
    }
}