package io.jpm.config;

import io.jpm.common.exception.cache.domain.SourceLocationCache;
import io.jpm.common.exception.cache.infra.SourceLocationCacheImpl;
import io.jpm.core.m_entity.dialect.MySqlDialect;
import io.jpm.core.m_entity.dialect.PostgreSqlDialect;
import io.jpm.core.m_entity.dialect.SqlDialect;

import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.cache.RepoMetaRegistryImpl;
import io.jpm.core.jpm_repository.domain.cache.RepoRelationRegistryImpl;

import java.util.Map;


public class AppConfig {

    private static SqlDialect sqlDialect;


    public static String MAPPER_NAME_SPACE = "dev.sj.jqm.mapper.";








    public static void sqlDialectInit(Map<String, String> options) {


        if(options.get("dbType").equals("MYSQL") )
        {sqlDialect = new MySqlDialect();}
        else {sqlDialect = new PostgreSqlDialect();}
    }




    public  static SqlDialect getSqlDialectImpl()
    {

        return sqlDialect;
    }





}
