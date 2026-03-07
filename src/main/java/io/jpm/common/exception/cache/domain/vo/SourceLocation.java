package io.jpm.common.exception.cache.domain.vo;

import javax.lang.model.element.Element;

public abstract class SourceLocation {

    public abstract String getClassName();
    public abstract long getLineNumber();
    public abstract Element getElement();
    public abstract String getExpression();
}