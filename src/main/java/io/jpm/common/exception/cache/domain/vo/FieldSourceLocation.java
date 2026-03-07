package io.jpm.common.exception.cache.domain.vo;

import javax.lang.model.element.Element;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(builder = "builder")
public abstract class FieldSourceLocation extends SourceLocation {



    public  abstract String getClassName();
    public abstract String getFieldName();
    public abstract long getLineNumber();
    public abstract Element getElement();


    public static ImmutableFieldSourceLocation.Builder builder() {
        return ImmutableFieldSourceLocation.builder();
    }
}