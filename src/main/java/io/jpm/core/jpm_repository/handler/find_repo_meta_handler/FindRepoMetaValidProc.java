package io.jpm.core.jpm_repository.handler.find_repo_meta_handler;

import io.jpm.common.exception.ErrorCode;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils.MethodRefUtil;
import io.jpm.core.jpm_repository.utils.ColumnResolver;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.valid.policy.JoinNodeValidatorPolicyV2;

import java.util.Arrays;
import java.util.List;



public class FindRepoMetaValidProc {

    private static final List<String> CONDITION_COMMANDS = Arrays.asList("where", "and", "or");
    private static final List<String> UPDATE_COMMANDS    = Arrays.asList("set", "setRaw");
    private static final List<String> JOIN_COMMANDS      = Arrays.asList("innerJoin", "leftJoin", "rightJoin");

    private final RepoMetaRegistry repoMetaRegistry;
    private final EntityRelationRegistry entityRelationRegistry;
    private final ErrorTracker errorTracker;
    private final JoinNodeValidatorPolicyV2 joinValidator; // 기존 join 검증 재사용

    public FindRepoMetaValidProc(BuildTimeMetadataCache cache, ErrorTracker errorTracker, ColumnResolver columnResolver) {
        this.repoMetaRegistry       = cache.getRepoMetaRegistry();
        this.entityRelationRegistry = cache.getEntityRelationRegistry();
        this.errorTracker           = errorTracker;
        this.joinValidator          = new JoinNodeValidatorPolicyV2(cache, errorTracker, columnResolver);
    }

    // ==========================================
    // Public API
    // ==========================================
    public void validate(MethodMeta methodMeta) {




        for (DslStatement stmt : methodMeta.getStatements()) {

            validateStatement(stmt);
        }


    }

    // ==========================================
    // Private - 분기
    // ==========================================
    private void validateStatement(DslStatement stmt) {
        String command = stmt.getCommand();

        errorTracker.setChainMethodName(command);

        LogPrinter.info("Validating " + command);
        if (CONDITION_COMMANDS.contains(command)) {
            validateCondition(stmt);
        } else if (UPDATE_COMMANDS.contains(command)) {
            validateUpdate(stmt);
        } else if (JOIN_COMMANDS.contains(command)) {
            validateJoin(stmt); // JoinNodeValidatorPolicyV2에 위임
        }

        if (stmt.getSubStatements() != null) {
            for (DslStatement sub : stmt.getSubStatements()) {
                validateStatement(sub);
            }
        }
    }

    // ==========================================
    // where, and, or
    // arg(0) = "OrderEntity::getStatus"
    // arg(1) = "="
    // arg(2) = "2" or "'John'"
    // ==========================================
    private void validateCondition(DslStatement stmt) {

        LogPrinter.info("validateCondition stmt: " + stmt.toString());
        String columnRef = stmt.getArg(0);
        String value     = stmt.getArg(2);

        if (shouldSkip(value)) return;

        String fieldType = resolveFieldType(columnRef);

        if (fieldType == null) return;

        String valueType = inferTypeFromValue(value);
        if (valueType == null) return;

        checkTypeMismatch(fieldType, valueType);

        LogPrinter.info("chainMethodName: " + errorTracker.getChainMethodName() + ", errorTracker className: "+errorTracker.getClassName() + ", errorTracker methodName: "+errorTracker.getMethodName());
    }

    // ==========================================
    // set, setRaw
    // arg(0) = "OrderEntity::getStatus"
    // arg(1) = "2" or "'John'"
    // ==========================================
    private void validateUpdate(DslStatement stmt) {
        String columnRef = stmt.getArg(0);
        String value     = stmt.getArg(1);

        if (shouldSkip(value)) return;

        String fieldType = resolveFieldType(columnRef);
        if (fieldType == null) return;

        String valueType = inferTypeFromValue(value);
        if (valueType == null) return;

        checkTypeMismatch(fieldType, valueType);
    }

    // ==========================================
    // innerJoin, leftJoin, rightJoin
    // arg(0) = "OrderItemEntity.class"
    // arg(1) = "OrderEntity::getId"
    // arg(2) = "OrderItemEntity::getOrderId"
    // → JoinNodeValidatorPolicyV2에 위임
    // ==========================================

    private void validateJoin(DslStatement stmt) {
        String leftCol  = stmt.getArg(1);
        String rightCol = stmt.getArg(2);
        if (leftCol == null || rightCol == null) return;
        LogPrinter.info("validateJoin leftCol: " + leftCol + ", rightCol: " + rightCol);
        joinValidator.validateJoinType(stmt.getCommand(), leftCol, rightCol);
    }

    // ==========================================
    // Private - 공통 유틸
    // ==========================================
    private boolean shouldSkip(String value) {
        return value == null
                || value.startsWith("#{")
                || "NUMERIC_EXPRESSION".equals(value);
    }

    private String resolveFieldType(String columnRef) {
        if (columnRef == null) return null;

        String cleaned = columnRef.contains("|")
                ? columnRef.split("\\|")[columnRef.split("\\|").length - 1]
                : columnRef;

        if (!cleaned.contains("::")) return null;

        String[] parts    = cleaned.split("::");
        String entityName = parts[0].trim();
        String fieldName  = convertGetterToField(parts[1].trim());

        EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(entityName);
        if (entityMeta == null) return null;

        String rawType = entityMeta.getFieldType(fieldName);
        if (rawType == null) return null;

        String normalized = normalizeFieldType(rawType);

        if ("FK".equals(normalized)) {
            return entityRelationRegistry.resolveFkType(entityName, fieldName);
        }

        return normalized;
    }

    private String inferTypeFromValue(String value) {
        if (value.startsWith("'") && value.endsWith("'")) return "STRING";
        if (value.matches("-?\\d+"))                      return "INTEGER";
        if (value.matches("-?\\d+[Ll]"))                  return "LONG";
        if (value.matches("-?\\d+\\.\\d+"))               return "DOUBLE";
        if ("true".equalsIgnoreCase(value)
                || "false".equalsIgnoreCase(value))              return "BOOLEAN";
        return null;
    }

    private void checkTypeMismatch(String fieldType, String valueType) {


        LogPrinter.info("checkTypeMismatch: fieldType: " + fieldType + ", valueType: " + valueType);
        if ("LONG".equals(fieldType) && "INTEGER".equals(valueType)) return;
        if (!fieldType.equals(valueType)) {
            LogPrinter.info("checkTypeMismatch: fieldType: " + fieldType + ", valueType: " + valueType + " chainMethodName " + errorTracker.getChainMethodName());
            errorTracker.addErrorInfo(ErrorCode.CONDITION_TYPE_MISMATCH);
        }
    }

    private String convertGetterToField(String methodName) {
        return MethodRefUtil.convertGetterToField(methodName);
    }

    private String normalizeFieldType(String rawType) {
        switch (rawType.toUpperCase()) {
            case "INTEGER":   return "INTEGER";
            case "LONG":      return "LONG";
            case "FK":        return "FK";
            case "FLOAT":
            case "DOUBLE":    return "DOUBLE";
            case "BOOLEAN":   return "BOOLEAN";
            case "STRING":
            case "TEXT":
            case "JSON":
            case "UUID_V_7":
            case "LOCAL_DATE":
            case "LOCAL_DATE_TIME": return "STRING";
            default:          return rawType.toUpperCase();
        }
    }
}
