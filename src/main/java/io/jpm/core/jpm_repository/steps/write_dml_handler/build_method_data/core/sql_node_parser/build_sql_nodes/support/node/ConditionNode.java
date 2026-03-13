package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node;


import io.jpm.core.jpm_repository.domain.model.BuildContext;


import io.jpm.core.jpm_repository.utils.ColumnResolver;


public class ConditionNode implements SqlNode {
    private final String column;   // Java 필드명 또는 "별칭.필드명"
    private final String operator; // =, !=, LIKE, IN 등
    private final Object value;    // 비교할 값 (String, Number, 또는 ?)
    private final String logicOperator;
    private final ColumnResolver columnResolver;

    public ConditionNode(String column, String operator, Object value, String logicOperator, ColumnResolver columnResolver) {
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.logicOperator = logicOperator;
        this.columnResolver = columnResolver;
    }


    public String toSql(BuildContext ctx) {
        String resolvedColumn = resolveSelectColumn(this.column, ctx);
        String formattedValue = formatValue(this.value);
        return resolvedColumn + " " + this.operator + " " + formattedValue;
    }

    @Override
    public void apply(BuildContext ctx) {
        // WhereClauseNode가 이 노드의 toSql()과 logicOperator를 사용해 조립할 것입니다.


    }

    // getter 추가 (WhereClauseNode에서 AND/OR 판단용)
    public String getLogicOperator() {
        return logicOperator;
    }

    // -------------------------------------------------------------------------
    // 내부 헬퍼 메서드 (JoinNode의 로직과 유사)
    // -------------------------------------------------------------------------

    private String resolveSelectColumn(String colStr, BuildContext ctx) {
        // OrderItemEntity::getProductName 형태 처리

            return columnResolver.resolve(colStr, ctx);

    }



    private String formatValue(Object val) {
        if (val == null) return "NULL";

        String s = val.toString();

        // 이미 처리된 케이스

        if (s.startsWith("''") && s.endsWith("''")) {
            return s.replaceAll("^''|''$", "'");
        }

        if (s.startsWith("'") && s.endsWith("'")) return s;


        if (s.equals("?")) return s;
        if (s.contains("#{")) return s;

        // 숫자면 따옴표 없이
        if (s.matches("-?\\d+(\\.\\d+)?")) return s;
        if (s.matches("-?\\d+[Ll]")) return s.replaceAll("(?i)L", "");

        // 나머지(String 포함) 무조건 따옴표
        return s.replace("'", "''");
    }


}
