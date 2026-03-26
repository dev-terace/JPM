package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context;

import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;

public class DslCommandProcValidContext implements Context {

    private final DslStatement dslStatement;

    private final String className;
    private final String methodName;
    private String chainMethodName;
    private final int lineNumber;

    public String getChainMethodName() {
        return chainMethodName;
    }

    public void setChainMethodName(String chainMethodName) {
        this.chainMethodName = chainMethodName;
    }

    public DslStatement getDslStatement() {
        return dslStatement;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }


    public DslCommandProcValidContext(DslStatement dslStatement, String className, String methodName, int lineNumber) {
        this.dslStatement = dslStatement;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
    }


    @Override
    public String toString() {
        return "DslCommandProcValidContext{" +
                "className='"       + className       + '\'' +
                ", methodName='"    + methodName       + '\'' +
                ", chainMethodName='" + chainMethodName + '\'' +
                ", methodMeta="     + dslStatement       +
                '}';
    }

}
