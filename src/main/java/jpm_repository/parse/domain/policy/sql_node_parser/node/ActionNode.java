package jpm_repository.parse.domain.policy.sql_node_parser.node;


import jpm_repository.parse.domain.vo.BuildContext;

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
