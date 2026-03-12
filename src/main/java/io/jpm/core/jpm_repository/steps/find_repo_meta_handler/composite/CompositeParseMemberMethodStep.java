package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.composite;

import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.MethodParseContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.core.RegisterParamsStep;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.parse_method_body_step.ParseMethodBodyStep;

public class CompositeParseMemberMethodStep implements Step<MethodParseContext> {

    private final Step<MethodParseContext> registerParamsStep;
    private final Step<MethodParseContext> parseMethodBodyStep;

    public CompositeParseMemberMethodStep(GlobalRegistry globalRegistry, AstContext astContext) {
        this.registerParamsStep  = new RegisterParamsStep(globalRegistry.mapParamRegistry());
        this.parseMethodBodyStep = new ParseMethodBodyStep(globalRegistry, astContext);
    }

    @Override
    public void execute(MethodParseContext context) throws Exception {
        registerParamsStep.execute(context);
        parseMethodBodyStep.execute(context);
    }
}
