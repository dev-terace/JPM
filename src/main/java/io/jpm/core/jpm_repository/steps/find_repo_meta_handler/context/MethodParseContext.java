package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context;

import com.sun.source.tree.MethodTree;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;

import javax.lang.model.element.TypeElement;

public class MethodParseContext {
    private final TypeElement repoElement;
    private final MethodTree methodTree;
    private final MethodMeta methodMeta;

    public MethodParseContext(TypeElement repoElement, MethodTree methodTree, MethodMeta methodMeta) {
        this.repoElement = repoElement;
        this.methodTree  = methodTree;
        this.methodMeta  = methodMeta;
    }

    public TypeElement getRepoElement() { return repoElement; }
    public MethodTree getMethodTree()   { return methodTree; }
    public MethodMeta getMethodMeta()   { return methodMeta; }
}
