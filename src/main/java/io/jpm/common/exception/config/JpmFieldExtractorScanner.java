package io.jpm.common.exception.config;

import com.sun.source.tree.*;
import com.sun.source.util.TreePathScanner;
import io.jpm.config.AppConfig;
import io.jpm.common.exception.cache.domain.SourceLocationCache;
import io.jpm.common.exception.cache.domain.vo.FieldSourceLocation;
import io.jpm.common.utils.LogPrinter;

import javax.lang.model.element.Element;

public class JpmFieldExtractorScanner extends TreePathScanner<Void, Void> {

    private final SourceLocationCache cache;
    private final JpmToolbox toolbox;

    private String currentClassName;
    private String currentEntityName;
    private Element currentElement;

    public JpmFieldExtractorScanner(SourceLocationCache cache, JpmToolbox toolbox) {
        this.cache = cache;
        this.toolbox = toolbox;
    }

    public void init(Element currentElement) {
        this.currentElement = currentElement;
    }

    public void setClassName(String className) {
        this.currentClassName = className;
    }

    public void setEntityName(String entityName) {
        this.currentEntityName = entityName;
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        try {
            // ✅ MField 타입 필드만 스캔
            String typeName = node.getType().toString();
            if (!typeName.equals("MField")) return super.visitVariable(node, p);

            String fieldName = node.getName().toString();
            long line = toolbox.getLineNumber(node);

            FieldSourceLocation loc = FieldSourceLocation.builder()
                    .className(currentClassName)
                    .entityName(currentEntityName)
                    .fieldName(fieldName)
                    .lineNumber(line)
                    .element(currentElement)
                    .build();

            // ✅ 캐시에 저장
            cache.registerFieldLocation(currentClassName, fieldName, loc);

        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e.getMessage());
        }

        return super.visitVariable(node, p);
    }
}