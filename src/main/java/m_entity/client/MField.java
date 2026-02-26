package m_entity.client;

public class MField {

    // --- 1. 필드 선언 ---
    private final MFieldType type;
    private final String name;
    private final boolean primaryKey;
    private final boolean autoIncrement;
    private final boolean nullable;
    private final String defaultValue;
    private final int length;
    private final String targetClassName;
    private final String onDelete;

    // 🔥 [추가] 인덱스 관련 필드
    private final boolean index;  // 일반 인덱스 여부
    private final boolean unique; // 유니크 인덱스 여부

    // --- 2. 생성자 ---
    private MField(Builder builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.primaryKey = builder.primaryKey;
        this.autoIncrement = builder.autoIncrement;
        this.nullable = builder.nullable;
        this.defaultValue = builder.defaultValue;
        this.length = builder.length;
        this.targetClassName = builder.targetClassName;
        this.onDelete = builder.onDelete;

        // 🔥 [추가] 빌더에서 값 할당
        this.index = builder.index;
        this.unique = builder.unique;
    }

    public static Builder builder() {
        return new Builder();
    }

    // --- 3. Getter 메서드 ---
    public MFieldType getType() { return type; }
    public String getName() { return name; }
    public boolean isPrimaryKey() { return primaryKey; }
    public boolean isAutoIncrement() { return autoIncrement; }
    public boolean isNullable() { return nullable; }
    public String getDefaultValue() { return defaultValue; }
    public int getLength() { return length; }
    public String getParentClassName() { return targetClassName; }
    public String getOnDelete() { return onDelete; }

    // 🔥 [추가] Getter
    public boolean isIndex() { return index; }
    public boolean isUnique() { return unique; }


    // --- Builder Class ---
    public static class Builder {
        private MFieldType type;
        private String name;

        private boolean primaryKey = false;
        private boolean autoIncrement = false;
        private boolean nullable = true;
        private String defaultValue = null;
        private int length = 255;
        private String targetClassName = null;
        private String onDelete = OnDeleteType.NO_ACTION.getSql();

        // 🔥 [추가] 기본값 false
        private boolean index = false;
        private boolean unique = false;

        public Builder type(MFieldType type) { this.type = type; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder primaryKey(boolean val) { this.primaryKey = val; return this; }
        public Builder autoIncrement(boolean val) { this.autoIncrement = val; return this; }
        public Builder nullable(boolean val) { this.nullable = val; return this; }
        public Builder defaultValue(String val) { this.defaultValue = val; return this; }
        public Builder length(int val) { this.length = val; return this; }

        // FK 관련
        public Builder parent(Class<?> clazz) {
            this.targetClassName = clazz.getSimpleName();
            return this;
        }
        public Builder parent(String className) {
            this.targetClassName = className;
            return this;
        }
        public Builder onDelete(OnDeleteType onDeleteType) { this.onDelete = onDeleteType.getSql(); return this; }

        // 🔥 [추가] 인덱스 설정 메서드
        public Builder index(boolean val) {
            this.index = val;
            return this;
        }

        // 🔥 [추가] 유니크 설정 메서드
        public Builder unique(boolean val) {
            this.unique = val;
            // 보통 unique면 index 기능도 포함하므로, 명시적으로 index도 true로 해줄 수도 있습니다.
            // 하지만 DDL 생성기 로직 분리를 위해 여기선 값만 저장합니다.
            return this;
        }

        public MField build() {
            return new MField(this);
        }
    }
}