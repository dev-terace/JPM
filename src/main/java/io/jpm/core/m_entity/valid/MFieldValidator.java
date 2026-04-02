package io.jpm.core.m_entity.valid;

import io.jpm.api.MField;
import io.jpm.api.m_field_type.MFieldTypeEnum;
import io.jpm.common.exception.ErrorCode;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;

import java.util.Arrays;
import java.util.List;

public class MFieldValidator {

    private static final List<MFieldTypeEnum> UNSUITABLE_PK_TYPES = Arrays.asList(
            MFieldTypeEnum.FLOAT, MFieldTypeEnum.DOUBLE, MFieldTypeEnum.BOOLEAN,
            MFieldTypeEnum.JSON, MFieldTypeEnum.TEXT
    );

    private ErrorTracker errorTracker;

    public void validate(MField<?> field, ErrorTracker errorTracker) {
        this.errorTracker = errorTracker;

        LogPrinter.info("MFieldValidator Validating " + field.getName());


        this.errorTracker.setFieldName(field.getName());

        validatePrimaryKey(field);
        validateUuidConstraints(field);
        validateAutoIncrement(field);
        validateForeignKey(field);
        validateLogicalConstraints(field);
        validateDefaultValue(field);
    }



    private void validatePrimaryKey(MField<?> field) {
        if (!field.isPrimaryKey()) return;

        String prefix = "[PK_VALID] " + field.getName() + ": ";

        if (field.isNullable()) {


            errorTracker.addErrorInfo(ErrorCode.PK_MUST_NOT_NULL);

        }


        if (UNSUITABLE_PK_TYPES.contains(field.getType())) {

            errorTracker.addErrorInfo(ErrorCode.PK_TYPE_UNSUITABLE);

        /*    LogPrinter.info(prefix + "Type '" + field.getType() + "' is unsuitable for PK");*/
        }
    }

    private void validateUuidConstraints(MField field) {
        if (field.getType() == MFieldTypeEnum.UUID_V_7 && !field.isPrimaryKey()) {
            errorTracker.addErrorInfo(ErrorCode.UUID_V7_MUST_PK);
            LogPrinter.info("[UUID_ERR] " + field.getName() + ": UUID_V_7 must be Primary Key.");
        }
    }

    private  void validateAutoIncrement(MField field) {
        if (!field.isAutoIncrement()) return;

        String prefix = "[AUTO_INC] " + field.getName() + ": ";

        if (field.getType() != MFieldTypeEnum.INTEGER && field.getType() != MFieldTypeEnum.LONG) {
            errorTracker.addErrorInfo(ErrorCode.AUTO_INC_NOT_ALLOWED);
            LogPrinter.info(prefix + "Only INTEGER/LONG allowed");
        }

        if (field.getType() == MFieldTypeEnum.FK || field.getType() == MFieldTypeEnum.UUID_V_7) {
            errorTracker.addErrorInfo(ErrorCode.AUTO_INC_TYPE_INVALID);
            LogPrinter.info(prefix + "FK or UUID cannot be AutoIncrement");
        }
    }

    private  void validateForeignKey(MField field) {
        if (field.getType() == MFieldTypeEnum.FK && (field.getParentClassName() == null || field.getParentClassName().isEmpty())) {
            errorTracker.addErrorInfo(ErrorCode.FK_PARENT_MISSING);
            LogPrinter.info("[FK_ERR] " + field.getName() + ": Missing 'parent' attribute");
        }
    }

    private  void validateLogicalConstraints(MField field) {
        if (field.getType() == MFieldTypeEnum.BOOLEAN && field.isUnique()) {
            errorTracker.addErrorInfo(ErrorCode.UNIQUE_BOOLEAN_INVALID);
            LogPrinter.info("[LOGIC_ERR] " + field.getName() + ": Unique on Boolean is meaningless");
        }
    }

    private  void validateDefaultValue(MField field) {
        String defVal = field.getDefaultValue();
        if (defVal == null || defVal.isEmpty()) return;

        try {
            if (field.getType() == MFieldTypeEnum.INTEGER || field.getType() == MFieldTypeEnum.LONG) {
                Long.parseLong(defVal);
            } else if (field.getType() == MFieldTypeEnum.BOOLEAN) {
                if (!defVal.equalsIgnoreCase("true") && !defVal.equalsIgnoreCase("false")) throw new Exception();
            }
        } catch (Exception e) {
            errorTracker.addErrorInfo(ErrorCode.DEFAULT_VALUE_MISMATCH);
            LogPrinter.info("[TYPE_MIS] " + field.getName() + ": Default '" + defVal + "' unmatched with " + field.getType());
        }
    }
}