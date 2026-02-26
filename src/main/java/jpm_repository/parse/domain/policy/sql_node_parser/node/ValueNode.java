package jpm_repository.parse.domain.policy.sql_node_parser.node;


import jpm_repository.parse.domain.vo.BuildContext;
import jpm_repository.generator.infra.utils.ColumnResolver;

public class ValueNode implements SqlNode {
    private final String column;
    private final String value;

    public ValueNode(String column, String value) {
        this.column = column;
        this.value = value;
    }

    @Override
    public void apply(BuildContext ctx) {


        ctx.getInsertCols().add(ColumnResolver.resolve(column, ctx));
        ctx.getInsertVals().add(formatValue(value));
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

        return "'" + s.replace("'", "''") + "'";
    }
}
