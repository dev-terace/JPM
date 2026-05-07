package io.jpm.core.jpm_repository.runtime.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ResultMappingRegistry {

    private final ConcurrentHashMap<String, ResultMappingMeta> cache = new ConcurrentHashMap<>();

    public ResultMappingMeta getOrCreate(String key, Supplier<ResultMappingMeta> supplier) {
        return cache.computeIfAbsent(key, k -> supplier.get());
    }

    public void registerResultMapping(String resultMappingId, ResultMappingMeta resultMappingMeta) {
        cache.put(resultMappingId, resultMappingMeta);
    }

}
