package mq_repository.infra;

import mq_mapper.domain.vo.EntityMeta;
import mq_mapper.infra.SqlMapperBinder;
import mq_mapper.infra.repo.EntityMetaRegistry;

public class ColumnResolver {

    public static String resolve(String colStr, SqlMapperBinder.BuildContext ctx) {

        String[] aliasPart = colStr.split("\\.");
        boolean aliasFound = aliasPart.length > 1;
        String alias = aliasFound  ? aliasPart[0] + "." : "";

        System.out.println("aliasPart = " + aliasPart.length + " alias = " + colStr);

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


        EntityMeta entityMeta = EntityMetaRegistry.getEntityMeta(className);
        String tableName = EntityMetaRegistry.getTable(className);
        if (entityMeta == null || tableName == null) return fieldName;

        String colName = entityMeta.getColumn(fieldName);
        return isPrefix  ? tableName + "." + (colName != null ? colName : fieldName) :
                                             (colName != null ? colName : fieldName);
    }
}