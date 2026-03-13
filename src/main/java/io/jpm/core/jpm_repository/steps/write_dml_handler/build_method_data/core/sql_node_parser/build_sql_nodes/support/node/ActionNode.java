package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node;


import io.jpm.core.jpm_repository.domain.model.BuildContext;

public class ActionNode implements SqlNode {
    private final String action;

    public ActionNode(String action) {
        this.action = action;
    }

    @Override
    public void apply(BuildContext ctx) {
        ctx.setAction(this.action);
    }

    @Override public String toSql(BuildContext ctx) { return ""; }
}
