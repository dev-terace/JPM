package io.jpm.core.jpm_repository.runtime.core;

import io.jpm.api.TerraceQuery;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DslStatementArgResolver {

    /**
     * lambda 인스턴스를 받아 "ClassName::methodName" 형식으로 반환
     * 예: OrderEntity::getId
     */
    public static List<String> resolve(DslStatementV2 stmt) {
        return Optional.of(stmt)
                .map(DslStatementV2::getArgs)
                .orElse(Collections.emptyList())
                .stream()
                .map(arg -> {
                    if (arg == null) {
                        return "null";
                    }

                    if (arg instanceof TerraceQuery.MFieldRef) {
                        System.out.println("[BuildSqlNodesStepV2] arg : " + arg);

                        return String.valueOf(((TerraceQuery.MFieldRef<?, ?>) arg).describe());
                    }


                    return String.valueOf(arg);
                })
                .collect(Collectors.toList());
    }



}
