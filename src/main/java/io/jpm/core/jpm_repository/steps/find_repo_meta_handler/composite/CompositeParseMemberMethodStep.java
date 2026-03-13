package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.composite;

import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.CompositeStep;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.MethodParseContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.core.RegisterParamsStep;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.parse_method_body_step.ParseMethodBodyStep;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CompositeParseMemberMethodStep implements CompositeStep<MethodParseContext> {

    private final List<Step<MethodParseContext>> steps;

    public CompositeParseMemberMethodStep(GlobalRegistry globalRegistry, AstContext astContext) {
        steps = Arrays.asList(
                new RegisterParamsStep(globalRegistry.mapParamRegistry()),
                new ParseMethodBodyStep(globalRegistry, astContext)
        );
    }

    @Override
    public List<Step<MethodParseContext>> getSteps() {
        return steps;
    }




}
