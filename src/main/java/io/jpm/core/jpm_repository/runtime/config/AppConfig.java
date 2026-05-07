package io.jpm.core.jpm_repository.runtime.config;

import io.jpm.core.jpm_repository.domain.cache.RepoMetaRegistryImpl;
import io.jpm.core.jpm_repository.domain.cache.RepoRelationRegistryImpl;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.io.InputStream;

public class AppConfig {

    private static RepoMetaRegistry repoMetaRegistry;
    private static EntityRelationRegistry entityRelationRegistry;
    private static  ColumnResolver columnResolver;


    public static ColumnResolver getColumnResolver() {
        return new ColumnResolver(repoMetaRegistry);
    }

    public static RepoMetaRegistry getRepoMetaRegistry() {
        if (repoMetaRegistry == null) {
            repoMetaRegistry = loadRepoMetaRegistry();
        }
        return repoMetaRegistry;
    }

    public static EntityRelationRegistry getEntityRelationRegistry() {
        if (entityRelationRegistry == null)
        {
            entityRelationRegistry = loadEntityRelationRegistry();
        }
        return entityRelationRegistry;
    }

    private static RepoMetaRegistry loadRepoMetaRegistry() {
        try {
            InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("repo-meta.json");

            if (is == null) {
                throw new RuntimeException("repo-meta.json not found");
            }

            return RepoMetaRegistryImpl.fromJson(is);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static EntityRelationRegistry loadEntityRelationRegistry() {
        try {
            InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("entity-relation-meta.json");

            if (is == null) {
                throw new RuntimeException("entity-relation-meta.json");
            }


            return RepoRelationRegistryImpl.fromJson(is);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
