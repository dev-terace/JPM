package mq_repository.infra;

import mq_mapper.infra.SqlMapperBinder;
import mq_repository.domain.SqlNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GroupByNode implements SqlNode {
    private final List<String> columns;
    public GroupByNode(List<String> columns) { this.columns = columns; }

    @Override
    public void apply(SqlMapperBinder.BuildContext ctx) {


        List<String> resolveColumns = columns.stream()
                .map(s -> ColumnResolver.resolve(s, ctx))
                .collect(Collectors.toList());

        ctx.groupBys.add(String.join(", ", resolveColumns));


    }
    @Override public String toSql(SqlMapperBinder.BuildContext ctx) { return ""; }
}
