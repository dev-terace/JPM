package io.jpm.core.jpm_repository.runtime.core.terrace_query_executor;


import io.jpm.api.TerraceQuery;


@FunctionalInterface
public interface TRepoSupplier<T extends  TerraceQuery> {
    void get();




}
