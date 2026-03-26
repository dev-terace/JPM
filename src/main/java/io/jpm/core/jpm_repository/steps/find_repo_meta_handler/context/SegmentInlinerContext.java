package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context;

import com.sun.source.tree.MethodInvocationTree;
import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;

import javax.lang.model.element.TypeElement;

public class SegmentInlinerContext implements Context {

    private final MethodInvocationTree call;
    private final TypeElement repoElement;
    private final MapParamRegistryImpl mapParamRegistry;
    private final MethodMeta methodMeta;

    public SegmentInlinerContext(MethodInvocationTree call, TypeElement repoElement, MapParamRegistryImpl mapParamRegistry, MethodMeta methodMeta) {
        this.call = call;
        this.repoElement = repoElement;
        this.mapParamRegistry = mapParamRegistry;
        this.methodMeta = methodMeta;
    }


    public MethodInvocationTree getCall() {
        return call;
    }

    public TypeElement getRepoElement() {
        return repoElement;
    }

    public MapParamRegistryImpl getMapParamRegistry() {
        return mapParamRegistry;
    }

    public MethodMeta getMethodMeta() {
        return methodMeta;
    }
}
