package io.jpm.core.jpm_repository.parse.domain.policy.sql_node_parser.node;

import io.jpm.core.jpm_repository.parse.domain.vo.BuildContext;

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
