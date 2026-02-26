package config;

import exception.cache.domain.SourceLocationCache;
import exception.cache.infra.SourceLocationCacheImpl;
import m_entity.dialect.MySqlDialect;
import m_entity.dialect.PostgreSqlDialect;
import m_entity.dialect.SqlDialect;
import jpm_repository.parse.domain.policy.sql_mapper_binder.SqlMapperBinder;
import jpm_repository.parse.domain.policy.sql_mapper_binder.SqlMapperBinderImplV2;
import jpm_repository.parse.domain.cache.RepoMetaRegistry;
import jpm_repository.parse.domain.cache.EntityRelationRegistry;
import jpm_repository.parse.infra.RepoMetaRegistryImpl;
import jpm_repository.parse.infra.RepoRelationRegistryImpl;

import java.util.Map;


public class AppConfig {

    private static SqlDialect sqlDialect;


    public static String MAPPER_NAME_SPACE = "dev.sj.jqm.mapper.";

    private static final EntityRelationRegistry entityRelationRegistry = new RepoRelationRegistryImpl();
    private static final RepoMetaRegistry REPO_META_REGISTRY = new RepoMetaRegistryImpl();
    private static final SqlMapperBinder sqlMapperBinder = new SqlMapperBinderImplV2(REPO_META_REGISTRY);
    private static final SourceLocationCache sourceLocationCache = new SourceLocationCacheImpl();



    public static SourceLocationCache getSourceLocationCache() {
        return sourceLocationCache;
    }
    public static void sqlDialectInit(Map<String, String> options) {
        if(options.get("dbType").equals("MYSQL") )
        {
            sqlDialect = new MySqlDialect();
        }
        else
        {
            sqlDialect = new PostgreSqlDialect();
        }
    }

    public static EntityRelationRegistry getEntityRelationRegistry() {return entityRelationRegistry;}
    public static RepoMetaRegistry getEntityMetaRegistry() {
        return REPO_META_REGISTRY;
    }
    public static SqlMapperBinder getSqlMapperBinder() {
        return sqlMapperBinder;
    }

    public  static SqlDialect getSqlDialectImpl()
    {

        return sqlDialect;
    }





}
