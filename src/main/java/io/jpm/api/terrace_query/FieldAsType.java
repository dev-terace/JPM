package io.jpm.api.terrace_query;

public class FieldAsType {
    private final String rawArg;
    private final Class<?> type;


    public FieldAsType(String rawArg, Class<?> type) {
        this.rawArg = rawArg;
        this.type = type;
    }

    public String getRawArg() {
        return rawArg;
    }

    public Class<?> getType() {
        return type;
    }
}
