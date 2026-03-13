package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.utils;


import io.jpm.core.jpm_repository.domain.model.BuildContext;

/**
 * {@link BuildContext}에 수집된 절을 하나의 SQL 문자열로 조립합니다.
 *
 * <p>기존 거대한 {@code assembleSql()} switch 문을 액션 유형별 전략으로 분리했습니다.
 * 새로운 SQL 방언이 필요하면 {@link AssembleStrategy}를 구현하고
 * SqlAssemblerforAction(String)}에 등록만 하면 됩니다.
 */
public class SqlAssemblerUtil {

    private SqlAssemblerUtil() {}

    public static String assemble(BuildContext ctx) {
        String action = ctx.getAction().isEmpty() ? "SELECT" : ctx.getAction();
        return strategyFor(action).assemble(ctx);
    }

    private static AssembleStrategy strategyFor(String action) {
        switch (action) {
            case "UPDATE": return UpdateStrategy.INSTANCE;
            case "DELETE": return DeleteStrategy.INSTANCE;
            case "INSERT": return InsertStrategy.INSTANCE;
            default:       return SelectStrategy.INSTANCE;
        }
    }

    // =========================================================================
    // 전략 인터페이스
    // =========================================================================

    @FunctionalInterface
    interface AssembleStrategy {
        String assemble(BuildContext ctx);
    }

    // =========================================================================
    // SELECT
    // =========================================================================

    enum SelectStrategy implements AssembleStrategy {
        INSTANCE;

        @Override
        public String assemble(BuildContext ctx) {
            StringBuilder sql = new StringBuilder();
            String fromClause = ctx.getTables().isEmpty()
                    ? ctx.getTablePrefix()
                    : String.join(", ", ctx.getTables());

            sql.append("SELECT ").append(ctx.getColumns().isEmpty() ? "*" : ctx.getColumns())
                    .append("\nFROM ").append(fromClause);

            ctx.getJoins().forEach(j -> sql.append("\n").append(j));

            appendWhere(sql, ctx);
            appendClause(sql, "\nGROUP BY ", ctx.getGroupBys());
            appendClause(sql, "\nORDER BY ", ctx.getOrderBys());

            if (!ctx.getLimit().isEmpty())  sql.append("\nLIMIT ").append(ctx.getLimit());
            if (!ctx.getOffset().isEmpty()) sql.append("\nOFFSET ").append(ctx.getOffset());

            return sql.toString();
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    enum UpdateStrategy implements AssembleStrategy {
        INSTANCE;

        @Override
        public String assemble(BuildContext ctx) {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(ctx.getTablePrefix())
                    .append("\nSET ").append(String.join(", ", ctx.getSets()));
            appendWhere(sql, ctx);
            return sql.toString();
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    enum DeleteStrategy implements AssembleStrategy {
        INSTANCE;

        @Override
        public String assemble(BuildContext ctx) {
            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM ").append(ctx.getTablePrefix());
            appendWhere(sql, ctx);
            return sql.toString();
        }
    }

    // =========================================================================
    // INSERT
    // =========================================================================

    enum InsertStrategy implements AssembleStrategy {
        INSTANCE;

        @Override
        public String assemble(BuildContext ctx) {
            String cols = ctx.getInsertCols().isEmpty()
                    ? (ctx.getColumns() == null ? "" : ctx.getColumns())
                    : "(" + String.join(", ", ctx.getInsertCols()) + ")";
            String vals = "(" + String.join(", ", ctx.getInsertVals()) + ")";

            return "INSERT INTO " + ctx.getTablePrefix() + " "
                    + cols + "\nVALUES " + vals;
        }
    }

    // =========================================================================
    // 공통 헬퍼
    // =========================================================================

    private static void appendWhere(StringBuilder sql, BuildContext ctx) {
        if (!ctx.getWheres().isEmpty()) {
            sql.append("\nWHERE ").append(String.join(" AND ", ctx.getWheres()));
        }
    }

    private static void appendClause(StringBuilder sql, String prefix, java.util.List<String> items) {
        if (!items.isEmpty()) {
            sql.append(prefix).append(String.join(", ", items));
        }
    }
}