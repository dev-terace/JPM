package io.jpm.common.exception.cache.domain.vo;

import javax.lang.model.element.Element;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(builder = "builder")
public abstract class ChainSourceLocation extends SourceLocation {

    @Override public abstract String getClassName();
    @Override public abstract long getLineNumber();
    @Override public abstract Element getElement();
    @Override public abstract String getExpression();

    public abstract String getMethodName();
    public abstract String getChainMethodName();
    public abstract int getChainIndex();

    public static ImmutableChainSourceLocation.Builder builder() {
        return ImmutableChainSourceLocation.builder();
    }
}