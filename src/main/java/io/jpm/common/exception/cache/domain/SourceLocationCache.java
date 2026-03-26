package io.jpm.common.exception.cache.domain;

import io.jpm.common.exception.cache.domain.vo.ChainSourceLocation;
import io.jpm.common.exception.cache.domain.vo.FieldSourceLocation;
import io.jpm.common.exception.cache.domain.vo.MethodSourceLocation;

public interface SourceLocationCache {
    void registerFieldLocation(String entityName, String fieldName, FieldSourceLocation loc);
    void registerMethodLocation(String className, String methodName, MethodSourceLocation loc);
    void registerChainLocation(String className, String methodName, ChainSourceLocation loc);
    ChainSourceLocation popChainSourceLocation(String className, String methodName, String chainMethodName, int lineNumber);
    FieldSourceLocation popFieldSourceLocation(String entityName, String fieldName);
}
