package exception.cache.domain.vo;

import javax.lang.model.element.Element;

public abstract class SourceLocation {
    private final String className;
    private final long lineNumber;
    private final Element element; // IDE 이동용

    protected SourceLocation(String className, long lineNumber, Element element) {
        this.className = className;
        this.lineNumber = lineNumber;
        this.element = element;
    }

    public String getClassName() { return className; }
    public long getLineNumber() { return lineNumber; }
    public Element getElement() { return element; }
}