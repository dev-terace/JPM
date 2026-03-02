package io.jpm.core.m_entity.valid;

import io.jpm.api.MField;
import io.jpm.api.MFieldType;
import io.jpm.common.utils.LogPrinter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MFieldValidator {

    private static final List<MFieldType> UNSUITABLE_PK_TYPES = Arrays.asList(
            MFieldType.FLOAT, MFieldType.DOUBLE, MFieldType.BOOLEAN,
            MFieldType.JSON, MFieldType.TEXT
    );

    public static void validate(MField field) {
        validatePrimaryKey(field);
        validateUuidConstraints(field);
        validateAutoIncrement(field);
        validateForeignKey(field);
        validateLogicalConstraints(field);
        validateDefaultValue(field);
    }

    @Deprecated
    public static void indexTypeValidate(Map<String, List<MField>> parsedVariablesCache) {
        for (List<MField> pkFields : parsedVariablesCache.values()) {
            MFieldType childPkType = null;
            String parentClassName = null;

            for (MField child : pkFields) {
                if (child.isPrimaryKey()) childPkType = child.getType();
                if (child.getType() == MFieldType.FK) parentClassName = child.getParentClassName();
            }

            if (parentClassName == null) continue;

            List<MField> parentMFields = parsedVariablesCache.get(parentClassName);
            if (parentMFields == null) continue;

            for (MField parent : parentMFields) {
                if (parent.isPrimaryKey()) {
                    if (childPkType != parent.getType()) {
                        // ✅ 인자값 하나로 합침
                        LogPrinter.info("[PK_MISMATCH] Field '" + parent.getName() + "' type '" + parent.getType() + "' is invalid for indexing.");
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private static void validatePrimaryKey(MField field) {
        if (!field.isPrimaryKey()) return;

        String prefix = "[PK_VALID] " + field.getName() + ": ";

        if (field.isNullable()) {
            LogPrinter.info(prefix + "Primary Key must be 'nullable=false'");
        }

        if (UNSUITABLE_PK_TYPES.contains(field.getType())) {
            LogPrinter.info(prefix + "Type '" + field.getType() + "' is unsuitable for PK");
        }
    }

    private static void validateUuidConstraints(MField field) {
        if (field.getType() == MFieldType.UUID_V_7 && !field.isPrimaryKey()) {
            LogPrinter.info("[UUID_ERR] " + field.getName() + ": UUID_V_7 must be Primary Key.");
        }
    }

    private static void validateAutoIncrement(MField field) {
        if (!field.isAutoIncrement()) return;

        String prefix = "[AUTO_INC] " + field.getName() + ": ";

        if (field.getType() != MFieldType.INTEGER && field.getType() != MFieldType.LONG) {
            LogPrinter.info(prefix + "Only INTEGER/LONG allowed");
        }

        if (field.getType() == MFieldType.FK || field.getType() == MFieldType.UUID_V_7) {
            LogPrinter.info(prefix + "FK or UUID cannot be AutoIncrement");
        }
    }

    private static void validateForeignKey(MField field) {
        if (field.getType() == MFieldType.FK && (field.getParentClassName() == null || field.getParentClassName().isEmpty())) {
            LogPrinter.info("[FK_ERR] " + field.getName() + ": Missing 'parent' attribute");
        }
    }

    private static void validateLogicalConstraints(MField field) {
        if (field.getType() == MFieldType.BOOLEAN && field.isUnique()) {
            LogPrinter.info("[LOGIC_ERR] " + field.getName() + ": Unique on Boolean is meaningless");
        }
    }

    private static void validateDefaultValue(MField field) {
        String defVal = field.getDefaultValue();
        if (defVal == null || defVal.isEmpty()) return;

        try {
            if (field.getType() == MFieldType.INTEGER || field.getType() == MFieldType.LONG) {
                Long.parseLong(defVal);
            } else if (field.getType() == MFieldType.BOOLEAN) {
                if (!defVal.equalsIgnoreCase("true") && !defVal.equalsIgnoreCase("false")) throw new Exception();
            }
        } catch (Exception e) {
            LogPrinter.info("[TYPE_MIS] " + field.getName() + ": Default '" + defVal + "' unmatched with " + field.getType());
        }
    }
}