package exception;

public enum ErrorCode {
    JOIN_DUP_PK("JOIN-001", "A JOIN condition cannot include two Primary Keys."),
    JOIN_TYPE_MISMATCH("JOIN-002", "Type mismatch between JOIN columns."),
    WHERE_TYPE_MISMATCH("WHERE-001", "Type mismatch between compared fields in WHERE clause.");

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
