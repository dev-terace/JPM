package io.jpm.api;

import org.immutables.value.Value;
import javax.annotation.Nullable;

@Value.Immutable
@Value.Style(get = {"get*", "is*"})
public abstract class MField {

    public abstract MFieldType getType();
    public abstract String getName();

    @Value.Default
    public boolean isPrimaryKey() { return false; }

    @Value.Default
    public boolean isAutoIncrement() { return false; }

    @Value.Default
    public boolean isNullable() { return true; }

    @Nullable
    public abstract String getDefaultValue();

    @Value.Default
    public int getLength() { return 255; }

    @Nullable
    public abstract String getParentClassName();

    @Value.Default
    public String getOnDelete() { return OnDeleteType.NO_ACTION.getSql(); }

    @Value.Default
    public boolean isIndex() { return false; }

    @Value.Default
    public boolean isUnique() { return false; }

    // ── Builder 위임 패턴 ────────────────────────
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        // ❌ 이게 문제 - 생성자에서 바로 ImmutableMField.builder() 호출
        // private final ImmutableMField.Builder delegate = ImmutableMField.builder();

        // ✅ 이렇게 lazy하게
        private ImmutableMField.Builder delegate;

        private ImmutableMField.Builder delegate() {
            if (delegate == null) {
                delegate = ImmutableMField.builder();
            }
            return delegate;
        }

        public Builder type(MFieldType type) { delegate().type(type); return this; }
        public Builder name(String name) { delegate().name(name); return this; }
        public Builder primaryKey(boolean val) { delegate().primaryKey(val); return this; }
        public Builder autoIncrement(boolean val) { delegate().autoIncrement(val); return this; }
        public Builder nullable(boolean val) { delegate().nullable(val); return this; }
        public Builder defaultValue(String val) { delegate().defaultValue(val); return this; }
        public Builder length(int val) { delegate().length(val); return this; }
        public Builder index(boolean val) { delegate().index(val); return this; }
        public Builder unique(boolean val) { delegate().unique(val); return this; }

        public Builder parent(Class<?> clazz) { delegate().parentClassName(clazz.getSimpleName()); return this; }
        public Builder parent(String className) { delegate().parentClassName(className); return this; }
        public Builder onDelete(OnDeleteType type) { delegate().onDelete(type.getSql()); return this; }

        public MField build() { return delegate().build(); }
    }
}