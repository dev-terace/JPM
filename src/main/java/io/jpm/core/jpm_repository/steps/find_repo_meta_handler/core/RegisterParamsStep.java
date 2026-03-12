package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.core;

import com.sun.source.tree.VariableTree;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.MethodParseContext;

public class RegisterParamsStep implements Step<MethodParseContext> {

    private final MapParamRegistryImpl mapParamRegistry;

    public RegisterParamsStep(MapParamRegistryImpl mapParamRegistry) {
        this.mapParamRegistry = mapParamRegistry;
    }

    @Override
    public void execute(MethodParseContext context) {
        MethodMeta methodMeta = context.getMethodMeta();
        for (VariableTree param : context.getMethodTree().getParameters()) {
            String paramName = param.getName().toString();
            String paramType = param.getType().toString();
            methodMeta.addParameter(paramName, paramType);
            mapParamRegistry.registerParam(paramName, paramType);
        }
    }
}
