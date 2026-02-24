package mq_repository.infra.node;

import mq_mapper.domain.policy.prev.SqlMapperBinderImpl;
import mq_repository.domain.BuildContext;
import mq_repository.domain.SqlNode;
import mq_repository.infra.utils.ColumnResolver;

import java.util.List;
import java.util.stream.Collectors;

public class GroupByNode implements SqlNode {
    private final List<String> columns;
    public GroupByNode(List<String> columns) { this.columns = columns; }

    @Override
    public void apply(BuildContext ctx) {


        List<String> resolveColumns = columns.stream()
                .map(s -> ColumnResolver.resolve(s, ctx))
                .collect(Collectors.toList());

        ctx.getGroupBys().add(String.join(", ", resolveColumns));


    }
    @Override public String toSql(BuildContext ctx) { return ""; }
}
