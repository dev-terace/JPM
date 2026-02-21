package mq_repository.infra;


import mq_repository.domain.SqlNode;



import mq_mapper.infra.repo.EntityMetaRegistry;
import mq_mapper.infra.SqlMapperBinder;

import mq_mapper.domain.vo.EntityMeta;



public class ConditionNode implements SqlNode {
    private final String column;   // Java 필드명 또는 "별칭.필드명"
    private final String operator; // =, !=, LIKE, IN 등
    private final Object value;    // 비교할 값 (String, Number, 또는 ?)
    private final String logicOperator;

    public ConditionNode(String logicOperator, String column, String operator, Object value) {
        this.logicOperator = logicOperator;
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    public String toSql(SqlMapperBinder.BuildContext ctx) {
        String resolvedColumn = resolveSelectColumn(this.column, ctx);
        String formattedValue = formatValue(this.value);
        return resolvedColumn + " " + this.operator + " " + formattedValue;
    }

    @Override
    public void apply(SqlMapperBinder.BuildContext ctx) {
        // WhereClauseNode가 이 노드의 toSql()과 logicOperator를 사용해 조립할 것입니다.
    }

    // getter 추가 (WhereClauseNode에서 AND/OR 판단용)
    public String getLogicOperator() {
        return logicOperator;
    }

    // -------------------------------------------------------------------------
    // 내부 헬퍼 메서드 (JoinNode의 로직과 유사)
    // -------------------------------------------------------------------------

    private String resolveSelectColumn(String colStr, SqlMapperBinder.BuildContext ctx) {
        // OrderItemEntity::getProductName 형태 처리

            return ColumnResolver.resolve(colStr, ctx);

    }

    private String convertGetterToField(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3)
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        if (methodName.startsWith("is") && methodName.length() > 2)
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        return methodName;
    }

    private String formatValue(Object val) {
        if (val == null) return "NULL";

        String s = val.toString();

        // 이미 처리된 케이스
        if (s.startsWith("'") && s.endsWith("'")) return s;
        if (s.equals("?")) return s;
        if (s.contains("#{")) return s;

        // 숫자면 따옴표 없이
        if (s.matches("-?\\d+(\\.\\d+)?")) return s;
        if (s.matches("-?\\d+[Ll]")) return s.replaceAll("(?i)L", "");

        // 나머지(String 포함) 무조건 따옴표
        return s.replace("'", "''");
    }

/*    private String getFieldType(String column, SqlMapperBinder.BuildContext ctx) {
        // "users_info.id" → "id" 추출
        String fieldName = column.contains(".") ? column.split("\\.")[1] : column;
        String tableName = column.contains(".")
                ? ctx.tableAliases.getOrDefault(column.split("\\.")[0], column.split("\\.")[0])
                : ctx.tablePrefix;

        EntityMeta meta = EntityMetaRegistry.getEntityMeta(tableName);
        if (meta == null) meta = EntityMetaRegistry.getByTableName(tableName);
        if (meta != null) return meta.getFieldType(fieldName);
        return null;
    }



    private String resolveArgToColumn(String arg, SqlMapperBinder.BuildContext ctx) {
        String[] parts = arg.split("::");
        String entityName = parts[0].trim();
        String fieldName = extractFieldName(parts[1].trim());

        EntityMeta meta = EntityMetaRegistry.getEntityMeta(entityName);
        if (meta != null) {
            String dbCol = meta.getColumn(fieldName);
            String alias = ctx.tableAliases.getOrDefault(meta.getTableName(), meta.getTableName());
            return alias + "." + (dbCol != null ? dbCol : fieldName);
        }
        return fieldName;
    }*/

/*    private String extractFieldName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        return methodName;
    }*/
}
