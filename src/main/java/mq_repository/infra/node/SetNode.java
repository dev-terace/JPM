package mq_repository.infra.node;

import mq_mapper.domain.policy.prev.SqlMapperBinderImpl;
import mq_repository.domain.BuildContext;
import mq_repository.domain.SqlNode;
import mq_repository.infra.utils.ColumnResolver;

public class SetNode implements SqlNode {
    private final String column;
    private final String value;

    public SetNode(String column, String value) {
        this.column = column;
        this.value = value;
    }

    @Override
    public void apply(BuildContext ctx) {

        ctx.getSets().add(ColumnResolver.resolve(column, ctx) + " = " + value);
    }

    @Override
    public String toSql(BuildContext ctx) { return ""; }

    /*private String formatValue(String s) {
    *//*    if (s == null) return "NULL";
        if (s.startsWith("'") && s.endsWith("'")) return s;
        if (s.equals("?")) return s;
        if (s.contains("#{")) return s;
        if (s.equals("TRUE") || s.equals("FALSE")) return s;
        if (s.matches("-?\\d+(\\.\\d+)?")) return s;
        if (s.matches("-?\\d+[Ll]")) return s.replaceAll("(?i)L", "");*//*
        return s;

        *//*return "'" + s.replace("'", "''") + "'";*//*
    }*/
}
