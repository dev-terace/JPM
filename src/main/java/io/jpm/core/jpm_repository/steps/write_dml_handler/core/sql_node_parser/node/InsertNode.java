package io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.node;

import io.jpm.core.jpm_repository.domain.model.BuildContext;

import java.util.List;

public class InsertNode implements SqlNode {
    private final List<String> args;

    public InsertNode(List<String> args) {
        this.args = args;
    }

    @Override
    public void apply(BuildContext ctx) {
        ctx.setAction("INSERT");

    }
    @Override public String toSql(BuildContext ctx) { return ""; }
}
