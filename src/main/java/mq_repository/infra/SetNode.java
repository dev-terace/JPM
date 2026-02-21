package mq_repository.infra;

import mq_mapper.infra.SqlMapperBinder;
import mq_repository.domain.SqlNode;

public class SetNode implements SqlNode {
    private final String column;
    private final String value;

    public SetNode(String column, String value) {
        this.column = column;
        this.value = value;
    }

    @Override
    public void apply(SqlMapperBinder.BuildContext ctx) {
        ctx.sets.add(ColumnResolver.resolve(column, ctx) + " = " + formatValue(value));
    }

    @Override
    public String toSql(SqlMapperBinder.BuildContext ctx) { return ""; }

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
