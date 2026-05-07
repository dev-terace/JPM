package io.jpm.core.jpm_repository.runtime.core;

import io.jpm.config.ast.Step;

import io.jpm.core.jpm_repository.domain.model.DslStatementV2;

import io.jpm.core.jpm_repository.runtime.context.SqlNodeParserContextV2;


public class DetectJoinPrefixStepV2 implements Step<SqlNodeParserContextV2> {


    @Override
    public void execute(SqlNodeParserContextV2 ctx) {
        for (DslStatementV2 stmt : ctx.getStatements()) {
            String cmd = stmt.getCommand();
            if (cmd.contains("Join") || "whereExistsGroup".equals(cmd) || "whereNotExistsGroup".equals(cmd)) {
                ctx.getBuildContext().markRequiresPrefix();
                return;
            }
        }
    }
}