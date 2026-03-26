package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context;

import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;

import java.util.List;

public class DslCommandProcContext implements Context {
    private final String command;
    private final List<String> rawArgs;
    private final MethodMeta methodMeta;
    private final String className;
    private final String methodName;
    private final int lineNumber;


    public String getCommand() {
        return command;
    }

    public List<String> getRawArgs() {
        return rawArgs;
    }

    public MethodMeta getMethodMeta() {
        return methodMeta;
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

    public DslCommandProcContext(String command, List<String> rawArgs, MethodMeta methodMeta, String className, String methodName, int lineNumber) {
        this.command = command;
        this.rawArgs = rawArgs;
        this.methodMeta = methodMeta;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
    }
}
