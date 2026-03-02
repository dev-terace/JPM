package io.jpm.common.exception.cache.infra;

import io.jpm.common.exception.cache.domain.vo.ChainSourceLocation;
import io.jpm.common.exception.cache.domain.vo.FieldSourceLocation;
import io.jpm.common.exception.cache.domain.vo.MethodSourceLocation;
import io.jpm.common.exception.cache.domain.SourceLocationCache;


import java.util.*;

public class SourceLocationCacheImpl implements SourceLocationCache {

    // Entity 필드: Map<EntityName, Map<FieldName, FieldSourceLocation>>
    private final Map<String, Map<String, FieldSourceLocation>> fieldLocationMap = new HashMap<>();

    // 메서드: Map<ClassName, Map<MethodName, MethodSourceLocation>>
    private final Map<String, Map<String, MethodSourceLocation>> methodLocationMap = new HashMap<>();

    // 체인: Map<ClassName, Map<MethodName, List<ChainSourceLocation>>>
    private final Map<String, Map<String, List<ChainSourceLocation>>> chainLocationMap = new HashMap<>();


    public ChainSourceLocation popChainSourceLocation(String className, String methodName, String chainMethodName) {

        List<ChainSourceLocation> chainSourceLocations = chainLocationMap.get(className).get(methodName);

        if (chainSourceLocations == null) return null;

        // 1. Iterator를 사용해서 리스트를 순회합니다.
        Iterator<ChainSourceLocation> it = chainSourceLocations.iterator();

        while (it.hasNext()) {
            ChainSourceLocation loc = it.next();

            // 2. 조건에 맞는 요소를 찾으면
            if (loc.getChainMethodName().equals(chainMethodName)) {
                it.remove(); // ✅ 현재 가리키고 있는 바로 그 요소를 안전하게 삭제!
                return loc;  // ✅ 찾은 값을 즉시 반환하고 종료
            }
        }


        throw new RuntimeException("ChainSourceLocation not found for " + className + "." + methodName + "." + chainMethodName);

    }

    public void registerFieldLocation(String entityName, String fieldName, FieldSourceLocation loc) {
        fieldLocationMap.computeIfAbsent(entityName, k -> new HashMap<>()).put(fieldName, loc);
    }

    public void registerMethodLocation(String className, String methodName, MethodSourceLocation loc) {
        methodLocationMap.computeIfAbsent(className, k -> new HashMap<>()).put(methodName, loc);
    }

    public void registerChainLocation(String className, String methodName, ChainSourceLocation loc) {
        chainLocationMap.computeIfAbsent(className, k -> new HashMap<>())
                .computeIfAbsent(methodName, k -> new ArrayList<>())
                .add(loc);
    }
}
