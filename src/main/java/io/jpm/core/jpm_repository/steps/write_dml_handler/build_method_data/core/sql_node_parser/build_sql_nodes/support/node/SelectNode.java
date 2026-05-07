package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node;


import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.ArrayList;
import java.util.List;


public class SelectNode implements SqlNode {
    private final List<String> columns;

    private final ColumnResolver columnResolver;


    public SelectNode(List<String> columns,  ColumnResolver columnResolver) {
        this.columns = columns;

        this.columnResolver = columnResolver;
    }

    @Override
    public void apply(BuildContext ctx) {
        List<String> resolved = new ArrayList<>();



        for (String col : columns) {

            resolved.add(columnResolver.resolve(col, ctx));
        }






        resolved = columnResolver.normalizeColumnName(resolved);
        LogPrinter.info("[SelectNode] resolved: " + resolved);




        ctx.setAction("SELECT");
        // 덮어쓰기 대신 누적
        if (ctx.getColumns().isEmpty()) {
            ctx.setColumns(String.join(", ", resolved));
        } else {
            ctx.setColumns(ctx.getColumns() + ", " + String.join(", ", resolved));
        }
    }





    @Override public String toSql(BuildContext ctx) { return ""; }
}