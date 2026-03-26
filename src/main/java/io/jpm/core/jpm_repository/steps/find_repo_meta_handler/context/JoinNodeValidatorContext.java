package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context;


import io.jpm.config.ast.Context;

public class JoinNodeValidatorContext implements Context {

    private final String command;
    private final String leftCol;
    private final String rightCol;
    private final String className;
    private final String methodName;
    private final String chainMethodName;
    private final int lineNumber;



    public JoinNodeValidatorContext(String command, String leftCol, String rightCol, String className, String methodName, String chainMethodName, int lineNumber) {
        this.command = command;
        this.leftCol = leftCol;
        this.rightCol = rightCol;
        this.className = className;
        this.methodName = methodName;
        this.chainMethodName = chainMethodName;
        this.lineNumber = lineNumber;
    }

    public String getCommand() {
        return command;
    }

    public String getLeftCol() {
        return leftCol;
    }

    public String getRightCol() {
        return rightCol;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getChainMethodName() {
        return chainMethodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
