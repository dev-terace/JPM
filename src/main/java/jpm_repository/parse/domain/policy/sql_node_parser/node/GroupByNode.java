package jpm_repository.parse.domain.policy.sql_node_parser.node;

import jpm_repository.parse.domain.vo.BuildContext;
import jpm_repository.generator.infra.utils.ColumnResolver;

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
