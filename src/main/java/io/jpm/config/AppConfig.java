package io.jpm.config;

import io.jpm.common.exception.cache.domain.SourceLocationCache;
import io.jpm.common.exception.cache.infra.SourceLocationCacheImpl;
import io.jpm.core.m_entity.dialect.MySqlDialect;
import io.jpm.core.m_entity.dialect.PostgreSqlDialect;
import io.jpm.core.m_entity.dialect.SqlDialect;
import io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_mapper_binder.SqlMapperBinder;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.cache.RepoMetaRegistryImpl;
import io.jpm.core.jpm_repository.domain.cache.RepoRelationRegistryImpl;

import java.util.Map;


public class AppConfig {

    private static SqlDialect sqlDialect;


    public static String MAPPER_NAME_SPACE = "dev.sj.jqm.mapper.";

    private static final EntityRelationRegistry entityRelationRegistry = new RepoRelationRegistryImpl();
    private static final RepoMetaRegistry REPO_META_REGISTRY = new RepoMetaRegistryImpl();

    private static final SourceLocationCache sourceLocationCache = new SourceLocationCacheImpl();



    public static SourceLocationCache getSourceLocationCache() {
        return sourceLocationCache;
    }


    public static void sqlDialectInit(Map<String, String> options) {


        if(options.get("dbType").equals("MYSQL") )
        {sqlDialect = new MySqlDialect();}
        else {sqlDialect = new PostgreSqlDialect();}
    }

    public static EntityRelationRegistry getEntityRelationRegistry() {return entityRelationRegistry;}
    public static RepoMetaRegistry getEntityMetaRegistry() {
        return REPO_META_REGISTRY;
    }
    public static SqlMapperBinder getSqlMapperBinder() {
        return null;
    }

    public  static SqlDialect getSqlDialectImpl()
    {

        return sqlDialect;
    }





}
