package mq_repository.infra;

import mq_mapper.infra.SqlMapperBinder;
import mq_repository.domain.SqlNode;

import java.util.List;

public class InsertNode implements SqlNode {
    private final List<String> args;

    public InsertNode(List<String> args) {
        this.args = args;
    }

    @Override
    public void apply(SqlMapperBinder.BuildContext ctx) {
        ctx.action = "INSERT";

    }
    @Override public String toSql(SqlMapperBinder.BuildContext ctx) { return ""; }
}
