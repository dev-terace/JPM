package jpm_repository.parse.infra;

import jpm_repository.parse.domain.cache.ParamRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * 메서드 파싱 중 사용하는 인자 컨텍스트.
 * 기존 raw Map<String, String>을 역할 있는 도메인 객체로 교체합니다.
 */
public class MapParamRegistryImpl implements ParamRegistry {

    private final Map<String, String> store = new HashMap<>();

    public void registerParam(String paramName, String paramType) {
        store.put("isParam_" + paramName, "true");
        store.put("paramType_" + paramName, paramType);
    }

    public void bind(String key, String value) {
        store.put(key, value);
    }

    public boolean isParam(String name) {
        return store.containsKey("isParam_" + name);
    }

    public boolean hasAlias(String name) {
        return store.containsKey("alias_" + name);
    }

    public String getAlias(String name) {
        return store.get("alias_" + name);
    }

    public boolean has(String key) {
        return store.containsKey(key);
    }

    public String get(String key) {
        return store.get(key);
    }

    /** 세그먼트 인라인 시 파라미터 이름 → 호출 시 전달된 실제 값 매핑 */
/*    public void mapSegmentArgs(Iterable<String> paramNames, Iterable<String> passedValues) {
        java.util.Iterator<String> names  = paramNames.iterator();
        java.util.Iterator<String> values = passedValues.iterator();
        while (names.hasNext() && values.hasNext()) {
            store.put(names.next(), values.next());
        }
    }*/
}