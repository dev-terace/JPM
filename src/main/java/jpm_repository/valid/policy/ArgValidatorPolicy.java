package jpm_repository.valid.policy;

import config.AppConfig;
import exception.ErrorCode;
import exception.ErrorCollector;
import jpm_repository.parse.domain.vo.ValueType;
import jpm_repository.parse.domain.vo.EntityMeta;
import jpm_repository.parse.domain.cache.RepoMetaRegistry;
import utils.LogPrinter;

import java.util.Arrays;


public class ArgValidatorPolicy {

    private String[] firstArgInfoTemp = null;
    private static final RepoMetaRegistry REPO_META_REGISTRY = AppConfig.getEntityMetaRegistry();
    private String commandTemp;


    public void saveFirstArgInfoIfMatched(String column, String command, int argIndex) {
        this.commandTemp = command;
        ErrorCollector.setChainMethodName(command);
        if (isSaveFirstArgInfoIfMatched(argIndex)) {
            LogPrinter.info("column : " + column);
            this.firstArgInfoTemp = splitEntityAndField(column);
        }
    }



    public ValueType validateArgIfMatched(int argIndex, String lastArgFieldType) {
        if (isValidateArgIfMatched(argIndex)) {
            if (lastArgFieldType != null &&lastArgFieldType.equals("NUMERIC_EXPRESSION")) {
                return null;
            }
            
            if(lastArgFieldType == null){
                return null; //#{} 는 검증 안함
            }


            return validateLiteralTypeTree(firstArgInfoTemp, lastArgFieldType);

        }
        return null;

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


    private ValueType validateLiteralTypeTree(String[] firstArgInfo, String lastArgFieldType) {



        String firstArgEntityName = firstArgInfo[0];
        String firstArgFieldName = firstArgInfo[1];
        LogPrinter.info("firstArgFieldName : " + firstArgFieldName + " lastArgFieldType"+ lastArgFieldType);
        EntityMeta entityMeta = REPO_META_REGISTRY.getEntityMeta(firstArgEntityName);
        if (entityMeta == null) return ValueType.RAW;

        String firstArgMFieldType = entityMeta.getFieldType(firstArgFieldName);
        if (firstArgMFieldType == null) return ValueType.RAW;

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

        if (firstArgFieldType.equals("LONG") && lastArgFieldType.equals("INTEGER")) return ValueType.RAW;

        if (!firstArgFieldType.equals(lastArgFieldType)) {
            ErrorCollector.addErrorInfo(ErrorCode.WHERE_TYPE_MISMATCH);
        }

        if(lastArgFieldType.equals("STRING")) return ValueType.QUOTED;

        return ValueType.RAW;
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



