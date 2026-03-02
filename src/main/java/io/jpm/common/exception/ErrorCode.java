package io.jpm.common.exception;

public enum ErrorCode {
    JOIN_DUP_PK("JOIN-001", "A JOIN condition cannot include two Primary Keys."),
    JOIN_TYPE_MISMATCH("JOIN-002", "Type mismatch between JOIN columns."),
    CONDITION_TYPE_MISMATCH("CONDITION-001", "Type mismatch between compared fields in WHERE clause."),
    M_FIELD_GETTER_NAMING_INVALID("M_FIELD-001", "Field '%s' must use getter '%s'"),
    PACKAGE_NOT_ACCESSIBLE("PACKAGE", "Forbidden access to internal package <'%s'>"),
    M_FIELD_MUST_PRIVATE("M_FIELD-002", "Field '%s' must be private");


    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }


    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
