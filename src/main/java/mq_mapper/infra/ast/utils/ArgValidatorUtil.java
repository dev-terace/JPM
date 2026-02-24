package mq_mapper.infra.ast;

import config.AppConfig;
import mq_mapper.domain.vo.EntityMeta;
import mq_mapper.infra.repo.EntityMetaRegistry;
import utils.LogPrinter;

import java.util.Arrays;


public class ArgValidator {

    private String[] firstArgInfoTemp = null;
    private static final EntityMetaRegistry entityMetaRegistry = AppConfig.getEntityMetaRegistry();
    private String commandTemp;


    public void saveFirstArgInfoIfMatched(String column, String command, int argIndex) {
        this.commandTemp = command;
        if (isSaveFirstArgInfoIfMatched(argIndex)) {
            LogPrinter.info("column : " + column);
            this.firstArgInfoTemp = splitEntityAndField(column);
        }
    }

    public void validateArgIfMatched(int argIndex, String lastArgFieldType) {
        if (isValidateArgIfMatched(argIndex)) {
            if (lastArgFieldType != null &&lastArgFieldType.equals("NUMERIC_EXPRESSION")) {
                return;
            }


            validateLiteralTypeTree(firstArgInfoTemp, lastArgFieldType);

        }

    }

    private boolean isSaveFirstArgInfoIfMatched(int argIndex) {
        boolean isWhereAndOrCondition = Arrays.asList("where", "and", "or").contains(commandTemp) && argIndex == 0;
        if (isWhereAndOrCondition) {
            return true;
        }
        boolean isUpdateSetCondition = Arrays.asList("set", "setRaw").contains(commandTemp) && argIndex == 0;
        if (isUpdateSetCondition) {
            return true;
        }

        return false;
    }

    private boolean isValidateArgIfMatched(int argIndex) {
        boolean isWhereAndOrCondition = Arrays.asList("where", "and", "or").contains(commandTemp) && argIndex == 2;
        if (isWhereAndOrCondition) {
            return true;
        }
        boolean isUpdateSetCondition = Arrays.asList("set", "setRaw").contains(commandTemp) && argIndex == 1;
        if (isUpdateSetCondition) {
            return true;
        }

        return false;
    }


    private void validateLiteralTypeTree(String[] firstArgInfo, String lastArgFieldType) {


        if (lastArgFieldType == null) return; // 리터럴이 아니면 검증 통과(변수 등)

        String firstArgEntityName = firstArgInfo[0];
        String firstArgFieldName = firstArgInfo[1];

        EntityMeta entityMeta = entityMetaRegistry.getEntityMeta(firstArgEntityName);
        if (entityMeta == null) return;

        String firstArgMFieldType = entityMeta.getFieldType(firstArgFieldName);
        if (firstArgMFieldType == null) return;

        String firstArgFieldType = "";

        switch (firstArgMFieldType.toUpperCase()) {
            case "INTEGER":
                firstArgFieldType = "INTEGER";
                break;
            case "LONG":
            case "FK":
                firstArgFieldType = "LONG";
                break;
            case "FLOAT":
            case "DOUBLE":
                firstArgFieldType = "DOUBLE";
                break;
            case "BOOLEAN":
                firstArgFieldType = "BOOLEAN";
                break;
            case "STRING":
            case "TEXT":
            case "JSON":
            case "UUID_V_7":
            case "LOCAL_DATE":
            case "LOCAL_DATE_TIME":
                firstArgFieldType = "STRING";
                break;
        }

        if (firstArgFieldType.equals("LONG") && lastArgFieldType.equals("INTEGER")) return;

        if (!firstArgFieldType.equals(lastArgFieldType)) {

            throw new RuntimeException(
                    "[타입 오류] " + firstArgEntityName + "." + firstArgFieldName
                            + " 은 " + firstArgMFieldType + " 인데 " + lastArgFieldType + " 를 사용했습니다.");
        }
    }


    private String[] splitEntityAndField(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String cleaned = raw.trim();
        if (cleaned.contains("|")) {
            String[] pipeParts = cleaned.split("\\|");
            cleaned = pipeParts[pipeParts.length - 1];
        }
        if (cleaned.contains("::")) {
            String[] parts = cleaned.split("::");
            String className = parts[0].trim();
            String fieldName = convertGetterToField(parts[1].trim());
            return new String[]{className, fieldName};
        }
        return null;
    }


    private String convertGetterToField(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
        return methodName;
    }

}



