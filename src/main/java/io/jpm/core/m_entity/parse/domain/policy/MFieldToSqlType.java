package io.jpm.core.m_entity.parse.domain.policy;


import io.jpm.api.MField;
import io.jpm.api.m_field_type.MFieldTypeEnum;
import io.jpm.core.m_entity.parse.domain.enums.ResolveType;

import java.util.Objects;


public class MFieldToSqlType {



    public static String resolveType(MField handleField) {

        ResolveType defaultResolveType = ResolveType.valueOf(handleField.getType().name());
        boolean ifPrimaryKeyReturnBigInt = handleField.isPrimaryKey()
                && (handleField.getType())
                == MFieldTypeEnum.INTEGER
                || handleField.getType() == MFieldTypeEnum.LONG;

        if (ifPrimaryKeyReturnBigInt) {

            return "BIGINT";
        }

        if (Objects.requireNonNull(handleField.getType()) == MFieldTypeEnum.STRING) {// 길이가 4000보다 크면 TEXT로 변환 (MySQL 등에서 유용)
            if (handleField.getLength() > 4000) {
                return "TEXT";
            }
            return "VARCHAR(" + handleField.getLength() + ")";
        }

        return defaultResolveType.getSqlType();

    }

}
