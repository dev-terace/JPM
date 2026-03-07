package io.jpm.common.exception;

public enum ErrorCode {
    JOIN_DUP_PK("JOIN-001", "A JOIN condition cannot include two Primary Keys."),
    JOIN_TYPE_MISMATCH("JOIN-002", "Type mismatch between JOIN columns."),
    CONDITION_TYPE_MISMATCH("CONDITION-001", "Type mismatch between compared fields in WHERE clause."),
    M_FIELD_GETTER_NAMING_INVALID("M_FIELD-001", "Field '%s' must use getter '%s'"),
    PACKAGE_NOT_ACCESSIBLE("PACKAGE", "Forbidden access to internal package <'%s'>"),
    M_FIELD_MUST_PRIVATE("M_FIELD-002", "Field '%s' must be private"),


    // PK 관련
    PK_MUST_NOT_NULL("PK-001", "Primary Key must be 'nullable=false'"),
    PK_TYPE_UNSUITABLE("PK-002", "This type is unsuitable for a Primary Key"),

    // UUID 관련
    UUID_V7_MUST_PK("UUID-001", "UUID_V_7 must be the Primary Key"),

    // Auto Increment 관련
    AUTO_INC_TYPE_INVALID("AUTO-001", "AutoIncrement is only allowed for INTEGER or LONG"),
    AUTO_INC_NOT_ALLOWED("AUTO-002", "FK or UUID cannot be AutoIncrement"),

    // Foreign Key 관련
    FK_PARENT_MISSING("FK-001", "Missing 'parent' attribute for Foreign Key"),

    // 논리 제약 및 타입 미스매치
    UNIQUE_BOOLEAN_INVALID("LOGIC-001", "Unique constraint on Boolean is meaningless"),
    DEFAULT_VALUE_MISMATCH("TYPE-001", "Default value does not match the field type");




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
