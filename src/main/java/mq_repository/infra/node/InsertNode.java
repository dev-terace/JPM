package mq_repository.infra.node;

import mq_repository.domain.BuildContext;
import mq_repository.domain.SqlNode;

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
