package io.jpm.common.exception;


import com.sun.source.util.Trees;
import javax.lang.model.element.Element;
import java.util.List;

/**
 * Interface for collecting and reporting errors during build time.
 * Implementations should manage internal state (current error, target element, etc.)
 * and provide chainable setters for convenience.
 */
public interface ErrorTracker {

    // --- Setter / Chaining Methods ---
    ErrorTracker setErr(ErrorCode code);
    ErrorTracker setFieldName(String fieldName);
    ErrorTracker setClassName(String className);

    ErrorTracker setMethodName(String methodName);

    ErrorTracker setChainMethodName(String chainMethodName);
    ErrorTracker setLineNumber(int lineNumber);
    ErrorTracker setTrees(Trees trees);

    ErrorTracker setErrorElement(Element element);

    ErrorTracker setExpression(String expression);

    // --- Add Error Info ---
    void addErrorInfo(ErrorCode err);
    void addErrorInfo(ErrorInfo errorInfo);

    // --- Getter Methods ---
    ErrorCode getErr();

    String getClassName();

    String getMethodName();

    String getChainMethodName();

    Element getErrorElement();

    String getExpression();

    List<ErrorInfo> getErrorInfos();

    // --- Reporting ---
    /**
     * Collects all currently accumulated errors and generates a report string.
     * Should handle missing source locations gracefully.
     */
    String reportChain();
    String reportField();
}
