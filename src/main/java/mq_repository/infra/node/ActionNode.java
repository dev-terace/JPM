package mq_repository.infra.node;


import mq_repository.domain.BuildContext;
import mq_repository.domain.SqlNode;

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
