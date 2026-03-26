package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context;

import com.sun.source.tree.BlockTree;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;

public class SegmentInlinerProcContext {
    private final BlockTree body;
    private final MapParamRegistryImpl argContext;
    private final MethodMeta methodMeta;
    private final String segmentClassName;
    private final String segmentMethodName;
    private final int lineNumber;

    public BlockTree getBody() {
        return body;
    }

    public MapParamRegistryImpl getArgContext() {
        return argContext;
    }

    public MethodMeta getMethodMeta() {
        return methodMeta;
    }

    public String getSegmentClassName() {
        return segmentClassName;
    }

    public String getSegmentMethodName() {
        return segmentMethodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public SegmentInlinerProcContext(BlockTree body, MapParamRegistryImpl argContext, MethodMeta methodMeta, String segmentClassName, String segmentMethodName, int lineNumber) {
        this.body = body;
        this.argContext = argContext;
        this.methodMeta = methodMeta;
        this.segmentClassName = segmentClassName;
        this.segmentMethodName = segmentMethodName;
        this.lineNumber = lineNumber;
    }
}
