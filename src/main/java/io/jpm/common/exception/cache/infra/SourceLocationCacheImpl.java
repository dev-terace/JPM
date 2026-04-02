package io.jpm.common.exception.cache.infra;

import io.jpm.common.exception.cache.domain.vo.ChainSourceLocation;
import io.jpm.common.exception.cache.domain.vo.FieldSourceLocation;
import io.jpm.common.exception.cache.domain.vo.MethodSourceLocation;
import io.jpm.common.exception.cache.domain.SourceLocationCache;
import io.jpm.common.utils.LogPrinter;


import java.util.*;
import java.util.function.Predicate;

public class SourceLocationCacheImpl implements SourceLocationCache {

    // Entity 필드: Map<EntityName, Map<FieldName, FieldSourceLocation>>
    private final Map<String, Map<String, FieldSourceLocation>> fieldLocationMap = new HashMap<>();

    // 메서드: Map<ClassName, Map<MethodName, MethodSourceLocation>>
    private final Map<String, Map<String, MethodSourceLocation>> methodLocationMap = new HashMap<>();

    // 체인: Map<ClassName, Map<MethodName, List<ChainSourceLocation>>>
    private final Map<String, Map<String, List<ChainSourceLocation>>> chainLocationMap = new HashMap<>();


    public ChainSourceLocation popChainSourceLocation(String className, String methodName, String chainMethodName, int lineNumber) {


        LogPrinter.info("chain location map" + chainLocationMap);
        List<ChainSourceLocation> locations = chainLocationMap.get(className).get(methodName);


        return popFromList(
                locations,
                loc -> loc.getChainMethodName().equals(chainMethodName)
                        && loc.getLineNumber() == lineNumber,

                String.format("ChainSourceLocation not found for %s.%s.%s", className, methodName, chainMethodName)
        );



    }


    public FieldSourceLocation popFieldSourceLocation(String className, String fieldName) {

        LogPrinter.info("field location map: " + fieldLocationMap);

        Map<String, FieldSourceLocation> innerMap = fieldLocationMap.get(className);
        if (innerMap == null) return null;

        // Map의 remove(key)는 삭제된 "값"을 반환합니다. 이게 곧 Pop입니다.
        FieldSourceLocation loc = innerMap.remove(fieldName);

        if (loc == null) {
            throw new RuntimeException(String.format("FieldSourceLocation not found for %s.%s", className, fieldName));
        }




        return loc;
    }



    private <T> T popFromList(List<T> list, Predicate<T> filter, String errorMessage) {
        if (list == null) return null;

        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            T item = it.next();
            if (filter.test(item)) {
                it.remove(); // 찾으면 즉시 삭제
                return item; // 삭제한 요소 반환
            }
        }

        // 못 찾았을 경우 예외 처리 (혹은 null 반환으로 설계 가능)
        throw new RuntimeException(errorMessage);
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
