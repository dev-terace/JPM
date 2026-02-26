package jpm_repository.generator.infra.utils;

import config.AppConfig;
import jpm_repository.parse.domain.vo.EntityMeta;
import jpm_repository.parse.domain.cache.RepoMetaRegistry;
import jpm_repository.parse.domain.vo.BuildContext;

public class ColumnResolver {

    private static final RepoMetaRegistry REPO_META_REGISTRY = AppConfig.getEntityMetaRegistry();


    public static String resolve(String colStr, BuildContext ctx) {

        String[] aliasPart = colStr.split("\\.");
        boolean aliasFound = aliasPart.length > 1;
        /* String alias = aliasFound  ? aliasPart[0] + "." : "";*/

        System.out.println("aliasPart = " + aliasPart.length + " alias = " + colStr);

        if (colStr.contains("::")) {
            String[] parts = colStr.split("::");
            String className = parts[0].trim();
            String methodName = parts[1].trim();
            boolean needsPrefix = ctx.isRequiresPrefix() || !ctx.getJoins().isEmpty();
            return convertGetterToField(className, methodName, needsPrefix);
        }

        return colStr;
    }

    public static String[] resolve(String colStr) {

        if (!colStr.contains("::")) {
            throw new RuntimeException("MField(::) 방식의 인자 값이 아닙니다.");
        }

        colStr = colStr.contains(".") ? colStr.split("\\.")[1] : colStr;
        ;
        String[] parts = colStr.split("::");
        String className = parts[0].trim();
        String methodName = parts[1].trim();
        String filedName = convertGetterToField(methodName);
        return new String[]{className, filedName};

    }


    private static String convertGetterToField(String methodName) {

        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        } else {
            throw new RuntimeException("[convertGetterToField] 알 수 없는 메서드명: " + methodName);
        }
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


        EntityMeta entityMeta = REPO_META_REGISTRY.getEntityMeta(className);
        String tableName = REPO_META_REGISTRY.getTable(entityMeta.getTableName());
        if (tableName == null) return fieldName;

        String colName = entityMeta.getColumn(fieldName);
        return isPrefix  ? tableName + "." + (colName != null ? colName : fieldName) :
                                             (colName != null ? colName : fieldName);
    }
}