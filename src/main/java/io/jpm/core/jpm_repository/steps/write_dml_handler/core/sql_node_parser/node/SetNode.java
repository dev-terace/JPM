package io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.node;

import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

public class SetNode implements SqlNode {
    private final String column;
    private final String value;

    private final ColumnResolver columnResolver;


    public SetNode(String column, String value, ColumnResolver columnResolver) {
        this.column = column;
        this.value = value;
        this.columnResolver = columnResolver;
    }

    @Override
    public void apply(BuildContext ctx) {

        ctx.getSets().add(columnResolver.resolve(column, ctx) + " = " + value);
    }

    @Override
    public String toSql(BuildContext ctx) { return ""; }

    private String formatValue(String s) {
        if (s == null) return "NULL";
        if (s.startsWith("'") && s.endsWith("'")) return s;
        if (s.equals("?")) return s;
        if (s.contains("#{")) return s;
        if (s.equals("TRUE") || s.equals("FALSE")) return s;
        if (s.matches("-?\\d+(\\.\\d+)?")) return s;
        if (s.matches("-?\\d+[Ll]")) return s.replaceAll("(?i)L", "");
        return s;

    }
}
