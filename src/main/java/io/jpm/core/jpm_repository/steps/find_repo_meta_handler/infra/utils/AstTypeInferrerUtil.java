package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils;

/**
 * 원시 문자열 값으로부터 SQL 타입을 추론합니다.
 * (기존 inferTypeFromString + lastArgToString 역할)
 */
public class AstTypeInferrerUtil {

    private AstTypeInferrerUtil() {}

    private static String inferFromString(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) return "UNKNOWN";

        String trimmed = rawValue.trim();

        if (trimmed.startsWith("'") && trimmed.endsWith("'")) return "STRING";
        if (trimmed.contains("'"))                             return "STRING";
        if (trimmed.equalsIgnoreCase("true")
                || trimmed.equalsIgnoreCase("false"))          return "BOOLEAN";


        boolean hasOperator = trimmed.matches(".*[+/*%-].*");
        if (hasOperator) {
            return "NUMERIC_EXPRESSION";
        }

        String onlyLiterals = trimmed
                .replaceAll("[a-zA-Z_][a-zA-Z0-9_]*", "")
                .replaceAll("[+/*%()-]", "")
                .trim();

        if (onlyLiterals.isEmpty() && hasOperator)              return "NUMERIC_EXPRESSION";
        if (onlyLiterals.contains("."))          return "DOUBLE";
        if (onlyLiterals.matches(".*\\d+.*"))    return "INTEGER";

        return "STRING";
    }

    /** ExpressionTree 에서 추출한 리터럴 Java 값을 SQL 타입 문자열로 변환합니다. */
    public static String inferFromLiteralValue(Object val, String command) {
        if (val instanceof String && command.contains("Raw"))                       return inferFromString(val.toString());
        if (val instanceof String)                      return "STRING";
        if (val instanceof Boolean)                      return "BOOLEAN";
        if (val instanceof Integer)                      return "INTEGER";
        if (val instanceof Long)                         return "LONG";
        if (val instanceof Double || val instanceof Float) return "DOUBLE";
        return null;
    }


}