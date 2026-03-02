package io.jpm.core.jpm_repository.parse.domain.policy.sql_node_parser.node;

import io.jpm.config.AppConfig;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.core.jpm_repository.parse.domain.vo.BuildContext;
import io.jpm.core.jpm_repository.generator.infra.utils.ColumnResolver;

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
        ctx.setAction("SELECT");
        // 덮어쓰기 대신 누적
        if (ctx.getColumns().isEmpty()) {
            ctx.setColumns(String.join(", ", resolved));
        } else {
            ctx.setColumns(ctx.getColumns() + ", " + String.join(", ", resolved));
        }
    }

/*   private String resolveSelectColumn(String colStr, SqlMapperBinderImpl.BuildContext ctx) {

        String[] aliasPart = colStr.split("\\.");
        boolean aliasFound = aliasPart.length > 1;
        String alias = aliasFound ? aliasPart[0] + "." : "";


        if (colStr.contains("::")) {
            String[] parts = colStr.split("::");
            String className = parts[0].trim();
            String methodName = parts[1].trim();
            boolean needsPrefix = ctx.requiresPrefix || !ctx.joins.isEmpty();
            return  alias + convertGetterToField(className, methodName, needsPrefix);
        }

        return colStr;
    }

    private static String convertGetterToField(String className, String methodName, boolean isPrefix) {
        String fieldName;
        if (methodName.startsWith("get") && methodName.length() > 3) {
            fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            fieldName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        } else {
            throw new RuntimeException("[convertGetterToField] 알 수 없는 메서드명: " + methodName);
        }

        if (!isPrefix) return fieldName;


        LogPrinter.info("[SelectNode] class Name and method: "  + className + "." + methodName);

        EntityMeta entityMeta = entityMetaRegistry.getEntityMeta(className);
        String tableName = entityMetaRegistry.getTable(className);
        if (entityMeta == null || tableName == null) return fieldName;

        String colName = entityMeta.getColumn(fieldName);
        return tableName + "." + (colName != null ? colName : fieldName);
    }*/


    @Override public String toSql(BuildContext ctx) { return ""; }
}