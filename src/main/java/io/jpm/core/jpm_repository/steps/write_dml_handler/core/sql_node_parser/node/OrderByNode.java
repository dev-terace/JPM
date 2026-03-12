package io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.node;

import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.List;
import java.util.stream.Collectors;

public class OrderByNode implements SqlNode {
    private final List<String> args;

    private final ColumnResolver columnResolver;

    public OrderByNode(List<String> args,  ColumnResolver columnResolver) { this.args = args;

        this.columnResolver = columnResolver;
    }


    @Override
    public void apply(BuildContext ctx) {
        // 마지막 인자가 ASC/DESC면 방향으로 처리
        String direction = "";
        List<String> colArgs = args;

        if (!args.isEmpty()) {
            String last = args.get(args.size() - 1).trim().toUpperCase();
            if (last.equals("ASC") || last.equals("DESC")) {
                direction = " " + last;
                colArgs = args.subList(0, args.size() - 1);
            }
        }

        List<String> resolved = colArgs.stream()
                .map(s -> columnResolver.resolve(s, ctx))
                .collect(Collectors.toList());

        ctx.getOrderBys().add(String.join(", ", resolved) + direction);
    }


    @Override public String toSql(BuildContext ctx) { return ""; }
}
