package io.jpm.core.jpm_repository.valid.domain;

public class ValidateFkTypeMatchVO {
    private final String leftEntityName;
    private final String leftFieldType;
    private final String rightEntityName;
    private final String rightFieldType;
    private final String leftFieldName;
    private final String rightFieldName;

    private ValidateFkTypeMatchVO(Builder builder) {
        this.leftEntityName  = builder.leftEntityName;
        this.leftFieldType   = builder.leftFieldType;
        this.rightEntityName = builder.rightEntityName;
        this.rightFieldType  = builder.rightFieldType;
        this.leftFieldName   = builder.leftFieldName;
        this.rightFieldName  = builder.rightFieldName;
    }

    public String getLeftEntityName()  { return leftEntityName; }
    public String getLeftFieldType()   { return leftFieldType; }
    public String getRightEntityName() { return rightEntityName; }
    public String getRightFieldType()  { return rightFieldType; }
    public String getLeftFieldName()   { return leftFieldName; }
    public String getRightFieldName()  { return rightFieldName; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String leftEntityName;
        private String leftFieldType;
        private String rightEntityName;
        private String rightFieldType;
        private String leftFieldName;
        private String rightFieldName;

        public Builder leftEntityName(String leftEntityName)   { this.leftEntityName = leftEntityName;   return this; }
        public Builder leftFieldType(String leftFieldType)     { this.leftFieldType = leftFieldType;     return this; }
        public Builder rightEntityName(String rightEntityName) { this.rightEntityName = rightEntityName; return this; }
        public Builder rightFieldType(String rightFieldType)   { this.rightFieldType = rightFieldType;   return this; }
        public Builder leftFieldName(String leftFieldName)     { this.leftFieldName = leftFieldName;     return this; }
        public Builder rightFieldName(String rightFieldName)   { this.rightFieldName = rightFieldName;   return this; }

        public ValidateFkTypeMatchVO build() { return new ValidateFkTypeMatchVO(this); }
    }

    @Override
    public String toString() {
        return "ValidateFkTypeMatchVO{" +
                "leftEntityName='"  + leftEntityName  + '\'' +
                ", leftFieldName='"  + leftFieldName   + '\'' +
                ", leftFieldType='"  + leftFieldType   + '\'' +
                ", rightEntityName='" + rightEntityName + '\'' +
                ", rightFieldName='" + rightFieldName  + '\'' +
                ", rightFieldType='" + rightFieldType  + '\'' +
                '}';
    }
}
