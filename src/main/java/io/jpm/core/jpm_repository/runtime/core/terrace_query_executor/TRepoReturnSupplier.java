package io.jpm.core.jpm_repository.runtime.core.terrace_query_executor;


import io.jpm.api.TerraceQuery;

public interface TRepoReturnSupplier<S extends TerraceQuery, R> {
    S get();
    Class<R> getResultType();
}