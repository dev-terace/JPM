package io.jpm.core.jpm_repository.steps.write_dml_handler;

import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;

public class ValidateErrorMainStep implements Step<JpmRepoContext> {

    @Override
    public void execute(JpmRepoContext ctx) {



        String errorMessage = ctx.getErrorTracker().reportChain();




        if (errorMessage != null) throw new IllegalArgumentException(errorMessage);
    }
}