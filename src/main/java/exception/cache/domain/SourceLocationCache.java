package exception.cache.domain;

import exception.cache.domain.vo.ChainSourceLocation;
import exception.cache.domain.vo.FieldSourceLocation;
import exception.cache.domain.vo.MethodSourceLocation;

public interface SourceLocationCache {
    void registerFieldLocation(String entityName, String fieldName, FieldSourceLocation loc);
    void registerMethodLocation(String className, String methodName, MethodSourceLocation loc);
    void registerChainLocation(String className, String methodName, ChainSourceLocation loc);
    ChainSourceLocation popChainSourceLocation(String className, String methodName, String chainMethodName);
}
