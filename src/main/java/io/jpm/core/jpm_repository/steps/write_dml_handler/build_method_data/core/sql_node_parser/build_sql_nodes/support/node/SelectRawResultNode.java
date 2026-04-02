package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node;

import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.ArrayList;
import java.util.List;

public class SelectRawResultNode extends SelectRawNode implements SqlNode{
    public SelectRawResultNode(List<String> rawArgs, ColumnResolver columnResolver) {
        super(rawArgs, columnResolver);
    }

 /*   private final String column;
    private final String alias;
    private final String resultType;
    public SelectRawResultNode(List<String> columns) {
        this.column = columns.get(0);
        this.alias = columns.get(1);
        this.resultType = columns.get(2);
    }

    @Override
    public void apply(BuildContext ctx) {
        String resolved = column + " AS " + alias;
        ctx.setAction("SELECT");
        if (ctx.getColumns().isEmpty()) {
            ctx.setColumns(resolved);
        }else {
            ctx.setColumns(ctx.getColumns() + ", " + resolved);
        }

    }

    @Override
    public String toSql(BuildContext ctx) {
        return "";
    }*/
}
