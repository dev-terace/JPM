package io.jpm.core.jpm_repository.parse.domain.vo;



public enum GroupType {
    AND(" AND "),
    OR(" OR ");

    private final String operator;

    GroupType(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
