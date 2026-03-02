package io.jpm.core.jpm_repository.processor.handler.handlerContext;

import io.jpm.core.jpm_repository.parse.domain.vo.MethodMeta;

import java.util.List;

public class SegmentInlinerCommand {

    private final String repoClassName;
    private final String fieldVarName;
    private final String segmentMethodName;
    private final MethodMeta methodMeta;
    private final List<String> passedArgs;

    public SegmentInlinerCommand(String repoClassName, String fieldVarName, String segmentMethodName, MethodMeta methodMeta, List<String> passedArgs) {
        this.repoClassName = repoClassName;
        this.fieldVarName = fieldVarName;
        this.segmentMethodName = segmentMethodName;
        this.methodMeta = methodMeta;
        this.passedArgs = passedArgs;
    }


    public String getRepoClassName() {
        return repoClassName;
    }

    public String getFieldVarName() {
        return fieldVarName;
    }

    public String getSegmentMethodName() {
        return segmentMethodName;
    }

    public MethodMeta getMethodMeta() {
        return methodMeta;
    }

    public List<String> getPassedArgs() {
        return passedArgs;
    }

}
