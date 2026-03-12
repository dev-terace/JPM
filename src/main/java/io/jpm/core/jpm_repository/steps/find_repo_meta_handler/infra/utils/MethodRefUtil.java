package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils;

/**
 * 메서드 참조(ClassName::method) 관련 유틸.
 */
public class MethodRefUtil {

    private MethodRefUtil() {}

    public static String extractFieldName(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String cleaned = raw.trim();
        if (cleaned.contains("|")) {
            String[] pipeParts = cleaned.split("\\|");
            cleaned = pipeParts[pipeParts.length - 1];
        }
        if (cleaned.contains("::")) {
            return convertGetterToField(cleaned.split("::")[1].trim());
        }

        return cleaned;
    }

    public static String convertGetterToField(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
        return methodName;
    }
}