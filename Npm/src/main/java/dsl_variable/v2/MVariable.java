package dsl_variable.v2;

// 패키지 경로 주의 (사용하시는 실제 경로에 맞추세요)
import dsl_variable.v2.ColumnType;

public class MVariable {

    // --- 1. 필드 선언 (모두 private final로 불변성 보장) ---
    private final ColumnType type;
    private final String name;
    private final boolean primaryKey;
    private final boolean autoIncrement;
    private final boolean nullable;
    private final String defaultValue;
    private final int length; // String용
    private final String targetClassName; // FK용
    private final String onDelete; // FK용

    // --- 2. 생성자 수정 (Builder 값을 this.필드에 할당) ---
    private MVariable(Builder builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.primaryKey = builder.primaryKey;

        // 🚨 [버그 수정] 기존 코드에서는 지역 변수에만 담고 사라졌음 -> 멤버 변수에 할당
        this.autoIncrement = builder.autoIncrement;
        this.nullable = builder.nullable;
        this.defaultValue = builder.defaultValue;
        this.length = builder.length;
        this.targetClassName = builder.targetClassName;
        this.onDelete = builder.onDelete;
    }

    public static Builder builder() {
        return new Builder();
    }

    // --- 3. Getter 메서드 구현 (Loader에서 사용함) ---
    public ColumnType getType() { return type; }
    public String getName() { return name; }
    public boolean isPrimaryKey() { return primaryKey; }
    public boolean isAutoIncrement() { return autoIncrement; }
    public boolean isNullable() { return nullable; }
    public String getDefaultValue() { return defaultValue; }
    public int getLength() { return length; }
    public String getTargetClassName() { return targetClassName; }
    public String getOnDelete() { return onDelete; }

    // --- Builder Class ---
    public static class Builder {
        // 필수값은 아니지만 기본값 설정
        private ColumnType type;
        private String name;

        // 기본값 설정 (중요)
        private boolean primaryKey = false;
        private boolean autoIncrement = false;
        private boolean nullable = true; // 기본적으로 NULL 허용
        private String defaultValue = null;
        private int length = 255;        // String 기본 길이
        private String targetClassName = null;
        private String onDelete = "NO ACTION";

        public Builder type(ColumnType type) { this.type = type; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder primaryKey(boolean val) { this.primaryKey = val; return this; }
        public Builder autoIncrement(boolean val) { this.autoIncrement = val; return this; }
        public Builder nullable(boolean val) { this.nullable = val; return this; }
        public Builder defaultValue(String val) { this.defaultValue = val; return this; }
        public Builder length(int val) { this.length = val; return this; }

        // FK 관련
        public Builder target(Class<?> clazz) {
            this.targetClassName = clazz.getSimpleName();
            return this;
        }
        public Builder target(String className) {
            this.targetClassName = className;
            return this;
        }
        public Builder onDelete(String val) { this.onDelete = val; return this; }

        public MVariable build() {
            return new MVariable(this);
        }
    }
}