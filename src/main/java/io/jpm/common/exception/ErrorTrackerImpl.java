package io.jpm.common.exception;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.jpm.config.AppConfig;
import io.jpm.common.exception.cache.domain.SourceLocationCache;
import io.jpm.common.exception.cache.domain.vo.ChainSourceLocation;
import org.gradle.api.GradleException;
import io.jpm.common.utils.LogPrinter;

import javax.lang.model.element.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ErrorTracker 인스턴스 구현체
 * 인스턴스 기반 + 체이닝 지원
 */
public class ErrorTrackerImpl implements ErrorTracker {

    private final SourceLocationCache cache;

    // 임시 상태
    private ErrorCode err;
    private String className;
    private String methodName;
    private String chainMethodName;
    private Element errorElement;
    private Trees trees;
    private String expression;

    private final List<ErrorInfo> errorInfos = new ArrayList<>();

    public ErrorTrackerImpl(SourceLocationCache cache) {
        this.cache = cache;
    }

    // --- Setter / 체이닝 ---
    @Override
    public ErrorTracker setErr(ErrorCode code) {
        this.err = code;
        return this;
    }

    @Override
    public ErrorTracker setClassName(String className) {
        this.className = className;
        return this;
    }

    @Override
    public ErrorTracker setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    @Override
    public ErrorTracker setChainMethodName(String chainMethodName) {
        this.chainMethodName = chainMethodName;
        return this;
    }

    @Override
    public ErrorTracker setTrees(Trees trees) {
        this.trees = trees;
        return this;
    }

    @Override
    public ErrorTracker setErrorElement(Element element) {
        this.errorElement = element;
        return this;
    }

    @Override
    public ErrorTracker setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    // --- Add ErrorInfo ---
    @Override
    public ErrorTracker addErrorInfo(ErrorCode err) {
        this.err = err; // 현재 임시 상태 업데이트
        errorInfos.add(ErrorInfo.builder()
                .chainMethodName(chainMethodName)
                .errorElement(errorElement)
                .className(className)
                .err(err)
                .methodName(methodName)
                .expression(expression)
                .build());
        return this;
    }

    // --- Getter ---
    @Override
    public ErrorCode getErr() { return err; }

    @Override
    public String getClassName() { return className; }

    @Override
    public String getMethodName() { return methodName; }

    @Override
    public String getChainMethodName() { return chainMethodName; }

    @Override
    public Element getErrorElement() { return errorElement; }

    @Override
    public String getExpression() { return expression; }

    @Override
    public List<ErrorInfo> getErrorInfos() {
        return Collections.unmodifiableList(errorInfos);
    }

    // --- Report ---
    @Override
    public String reportAll() {
        if (errorInfos.isEmpty()) return null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("\n================================================================================");
            sb.append("\n[JPM ERROR REPORT]");
            sb.append("\n================================================================================");

            for (ErrorInfo e : errorInfos) {

                ChainSourceLocation loc = null;
                try {
                    loc = cache.popChainSourceLocation(e.getClassName(), e.getMethodName(), e.getChainMethodName());
                    if (loc != null) {
                        this.errorElement = loc.getElement();
                        this.expression = loc.getExpression();
                    }
                } catch (Exception ignored) {}

                String code = (e.getErr() != null && e.getErr().getCode() != null) ? e.getErr().getCode() : "UNKNOWN";
                String desc = (e.getErr() != null && e.getErr().getMessage() != null) ? e.getErr().getMessage() : "";

                sb.append("\n\n▶ Error: ").append(code);
                sb.append("\n  Description: ").append(desc);
                sb.append("\n  Target: repository[").append(e.getClassName()).append("], Method[").append(e.getMethodName()).append("]");

                if (loc != null) {
                    String ideLink = buildAbsoluteLink(errorElement, loc.getLineNumber());
                    sb.append("\n").append(ideLink);
                    sb.append("\n  Details: Failed at '").append(String.format("(%s)'", expression));
                } else {
                    sb.append("Location not found for ")
                            .append(e.getClassName()).append(".")
                            .append(e.getMethodName()).append("\n")
                            .append(e.getChainMethodName()).append("\n");
                }
                sb.append("\n------------------------------------------------------------\n");
            }

            return sb.toString();

        } catch (Exception ex) {
            LogPrinter.exceptionInfo(ex);
            throw new GradleException(ex.getMessage());
        }
    }

    // --- IDE Link Helper ---
    private String buildAbsoluteLink(Element element, long lineNumber) {
        if (element == null) return "Location Unknown";
        try {
            TreePath path = trees.getPath(element);
            if (path == null) return "Location Unknown";

            File file = new File(path.getCompilationUnit().getSourceFile().toUri());
            String absolutePath = file.getAbsolutePath();

            return String.format("  at Click.error(%s:%d)", absolutePath, lineNumber);

        } catch (Exception e) {
            return "Path Error: " + e.getMessage();
        }
    }
}