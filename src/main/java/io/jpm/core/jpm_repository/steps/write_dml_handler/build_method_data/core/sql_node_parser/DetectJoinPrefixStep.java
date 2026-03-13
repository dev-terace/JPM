package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser;

import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context.SqlNodeParserContext;

public class DetectJoinPrefixStep implements Step<SqlNodeParserContext> {

    @Override
    public void execute(SqlNodeParserContext ctx) {
        for (DslStatement stmt : ctx.getStatements()) {
            String cmd = stmt.getCommand();
            if (cmd.contains("Join") || "whereExistsGroup".equals(cmd) || "whereNotExistsGroup".equals(cmd)) {
                ctx.getBuildContext().markRequiresPrefix();
                return;
            }
        }
    }
}