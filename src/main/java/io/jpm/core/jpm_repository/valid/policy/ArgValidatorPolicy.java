package io.jpm.core.jpm_repository.valid.policy;

import io.jpm.config.AppConfig;
import io.jpm.common.exception.ErrorCode;
import io.jpm.common.exception.ErrorCollector;
import io.jpm.core.jpm_repository.parse.domain.cache.EntityRelationRegistry;
import io.jpm.core.jpm_repository.parse.domain.vo.ValueType;
import io.jpm.core.jpm_repository.parse.domain.vo.EntityMeta;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.common.utils.LogPrinter;

import java.util.Arrays;
import java.util.List;

public class ArgValidatorPolicy {

    private static final RepoMetaRegistry repoMetaRegistry = AppConfig.getEntityMetaRegistry();
    private static final EntityRelationRegistry entityRelationRegistry = AppConfig.getEntityRelationRegistry();

    // 반복되는 명령어 그룹을 상수로 분리
    private static final List<String> CONDITION_COMMANDS = Arrays.asList("where", "and", "or");
    private static final List<String> UPDATE_COMMANDS = Arrays.asList("set", "setRaw");

    private String[] firstArgInfoTemp = null;
    private String commandTemp;

    // ==========================================
    // Public API (데이터 저장부)
    // ==========================================
    public void saveFirstArgInfoIfMatched(String column, String command, int argIndex) {
        this.commandTemp = command;
        ErrorCollector.setChainMethodName(command);

        if (isSaveFirstArgInfoIfMatched(argIndex)) {
            LogPrinter.info("column : " + column);
            this.firstArgInfoTemp = splitEntityAndField(column);
        }
    }

    // ==========================================
    // Public API (검증부)
    // ==========================================
    public ValueType validateArgIfMatched(int argIndex, String conditionValType) {
        if (!isValidateArgIfMatched(argIndex)) return null;
        if (shouldSkipValidation(conditionValType)) return null;

        return validateLiteralTypeTree(firstArgInfoTemp, conditionValType);
    }

    // ==========================================
    // Private - 조건 매칭 로직
    // ==========================================
    private boolean isSaveFirstArgInfoIfMatched(int argIndex) {
        return isCommandMatched(CONDITION_COMMANDS, 0, argIndex) ||
                isCommandMatched(UPDATE_COMMANDS, 0, argIndex);
    }

    private boolean isValidateArgIfMatched(int argIndex) {
        return isCommandMatched(CONDITION_COMMANDS, 2, argIndex) ||
                isCommandMatched(UPDATE_COMMANDS, 1, argIndex);
    }

    private boolean isCommandMatched(List<String> commands, int targetIndex, int currentIndex) {
        return commands.contains(commandTemp) && currentIndex == targetIndex;
    }

    private boolean shouldSkipValidation(String conditionValType) {
        // null 이거나(예: #{}) 특정 표현식이면 검증 생략
        return conditionValType == null || "NUMERIC_EXPRESSION".equals(conditionValType);
    }

    // ==========================================
    // Private - 문자열 파싱 로직
    // ==========================================
    private String[] splitEntityAndField(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;

        String cleaned = extractStringAfterPipe(raw.trim());
        return extractClassAndFieldName(cleaned);
    }

    private String extractStringAfterPipe(String cleaned) {
        if (!cleaned.contains("|")) return cleaned;
        String[] pipeParts = cleaned.split("\\|");
        return pipeParts[pipeParts.length - 1];
    }

    private String[] extractClassAndFieldName(String cleaned) {
        if (!cleaned.contains("::")) return null;
        String[] parts = cleaned.split("::");
        String className = parts[0].trim();
        String fieldName = convertGetterToField(parts[1].trim());
        return new String[]{className, fieldName};
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

    // ==========================================
    // Private - 핵심 검증 로직 (validateLiteralTypeTree) 분할
    // ==========================================
    private ValueType validateLiteralTypeTree(String[] firstArgInfo, String conditionValType) {
        if (firstArgInfo == null || firstArgInfo.length < 2) return ValueType.RAW;

        String entityName = firstArgInfo[0];
        String fieldName = firstArgInfo[1];
        LogPrinter.info("firstArgFieldName : " + fieldName + " conditionValInfo" + conditionValType);

        String rawFieldType = fetchRawFieldType(entityName, fieldName);
        if (rawFieldType == null) return ValueType.RAW;

        String resolvedType = resolveAndNormalizeType(entityName, fieldName, rawFieldType);

        checkTypeMismatch(resolvedType, conditionValType);

        return determineReturnValueType(conditionValType);
    }

    private String fetchRawFieldType(String entityName, String fieldName) {
        EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(entityName);
        return (entityMeta != null) ? entityMeta.getFieldType(fieldName) : null;
    }

    private String resolveAndNormalizeType(String entityName, String fieldName, String rawFieldType) {
        String normalizedType = normalizeFieldType(rawFieldType);
        LogPrinter.info("normalizedType : " + rawFieldType + " normalizedType : " + normalizedType);
        if ("FK".equals(normalizedType)) {
            return entityRelationRegistry.resolveFkType(entityName, fieldName);
        }
        return normalizedType;
    }

    private String normalizeFieldType(String rawType) {
        switch (rawType.toUpperCase()) {
            case "INTEGER":
                return "INTEGER";
            case "LONG":
                return "LONG";
            case "FK":
                return "FK";
            case "FLOAT":
            case "DOUBLE":
                return "DOUBLE";
            case "BOOLEAN":
                return "BOOLEAN";
            case "STRING":
            case "TEXT":
            case "JSON":
            case "UUID_V_7":
            case "LOCAL_DATE":
            case "LOCAL_DATE_TIME":
                return "STRING";
            default:
                return rawType.toUpperCase();
        }
    }

    private void checkTypeMismatch(String fieldType, String conditionValType) {
        // 호환 가능한 타입 (예: LONG 필드에 INTEGER 조건)은 예외 처리
        if ("LONG".equals(fieldType) && "INTEGER".equals(conditionValType)) {
            return;
        }

        if (!fieldType.equals(conditionValType)) {
            ErrorCollector.addErrorInfo(ErrorCode.CONDITION_TYPE_MISMATCH);
        }
    }

    private ValueType determineReturnValueType(String conditionValType) {
        return "STRING".equals(conditionValType) ? ValueType.QUOTED : ValueType.RAW;
    }
}