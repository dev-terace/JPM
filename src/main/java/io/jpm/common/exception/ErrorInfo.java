package io.jpm.common.exception;

import org.immutables.value.Value;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;

@Value.Immutable

public abstract class ErrorInfo {

    public abstract ErrorCode getErr();
    public abstract String getClassName();

    @Nullable public abstract String getExpression();
    @Nullable public abstract String getMethodName();
    @Nullable public abstract String getChainMethodName();
    @Nullable public abstract String getFieldName();
    @Nullable public abstract Element getErrorElement();

    public static ImmutableErrorInfo.Builder builder() {
        return ImmutableErrorInfo.builder();
    }
}