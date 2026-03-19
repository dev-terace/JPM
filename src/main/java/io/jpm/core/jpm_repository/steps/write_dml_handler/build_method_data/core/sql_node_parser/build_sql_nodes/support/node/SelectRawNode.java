package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node;

import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.ArrayList;
import java.util.List;

public class SelectRawNode implements SqlNode {
    private final String template;       // 예: "SUM(%s)", "COALESCE(%s, %s)"
    private final List<String> args;     // 예: ["orders.amount"], ["col1", "0"]
    private final ColumnResolver columnResolver;

    public SelectRawNode(List<String> rawArgs, ColumnResolver columnResolver) {
        // 첫 번째 인자가 포맷 템플릿, 나머지가 %s에 들어갈 값
        this.template       = rawArgs.isEmpty() ? "" : rawArgs.get(0);
        this.args           = rawArgs.size() > 1 ? rawArgs.subList(1, rawArgs.size()) : new ArrayList<>();
        this.columnResolver = columnResolver;
    }

    @Override
    public void apply(BuildContext ctx) {
        String resolved = buildSql(ctx);
        ctx.setAction("SELECT");
        if (ctx.getColumns().isEmpty()) {
            ctx.setColumns(resolved);
        } else {
            ctx.setColumns(ctx.getColumns() + ", " + resolved);
        }
    }

    @Override
    public String toSql(BuildContext ctx) { return ""; }

    private String buildSql(BuildContext ctx) {
        // 각 arg를 ColumnResolver로 변환 후 %s 자리에 순서대로 주입
        Object[] resolved = args.stream()
                .map(arg -> columnResolver.resolve(arg, ctx))
                .toArray();
        return String.format(template, resolved);
    }
}