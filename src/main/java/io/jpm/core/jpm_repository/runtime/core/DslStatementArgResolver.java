package io.jpm.core.jpm_repository.runtime.core;

import io.jpm.api.terrace_query.TerraceQuery;
import io.jpm.common.utils.CustomLogger;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DslStatementArgResolver {

    private static final Map<String, List<String>> RESOLVE_CACHE = new ConcurrentHashMap<>();
    private static final CustomLogger log = CustomLogger.getLogger(DslStatementArgResolver.class);

    public static List<String> resolve(DslStatementV2 stmt) {
        String cacheKey = toCacheKey(stmt);
        return RESOLVE_CACHE.computeIfAbsent(cacheKey, k -> resolveInternal(stmt));
    }

    private static String toCacheKey(DslStatementV2 stmt) {
        StringBuilder sb = new StringBuilder(stmt.getCommand()).append(":");
        List<Object> args = stmt.getArgs();
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof TerraceQuery.MFieldRef) {
                    sb.append(((TerraceQuery.MFieldRef<?, ?>) arg).describe());
                } else {
                    sb.append(arg);
                }
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private static List<String> resolveInternal(DslStatementV2 stmt) {
        return Optional.of(stmt)
                .map(DslStatementV2::getArgs)
                .orElse(Collections.emptyList())
                .stream()
                .map(arg -> {
                    if (arg == null) return "null";
                    if (arg instanceof TerraceQuery.MFieldRef) {
                        log.debug("arg : " + arg);
                        return String.valueOf(((TerraceQuery.MFieldRef<?, ?>) arg).describe());
                    }
                    return String.valueOf(arg);
                })
                .collect(Collectors.toList());
    }
}
