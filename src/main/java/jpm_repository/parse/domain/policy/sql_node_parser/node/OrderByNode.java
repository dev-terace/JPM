package jpm_repository.parse.domain.policy.sql_node_parser.node;

import jpm_repository.parse.domain.vo.BuildContext;
import jpm_repository.generator.infra.utils.ColumnResolver;

import java.util.List;
import java.util.stream.Collectors;

public class OrderByNode implements SqlNode {
    private final List<String> args;
    public OrderByNode(List<String> args) { this.args = args; }

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
                .map(s -> ColumnResolver.resolve(s, ctx))
                .collect(Collectors.toList());

        ctx.getOrderBys().add(String.join(", ", resolved) + direction);
    }


    @Override public String toSql(BuildContext ctx) { return ""; }
}
