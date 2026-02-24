package config;

import m_ddl_generator.dialect.MySqlDialect;
import m_ddl_generator.dialect.PostgreSqlDialect;
import m_ddl_generator.dialect.SqlDialect;
import mq_mapper.domain.policy.SqlMapperBinder;
import mq_mapper.domain.policy.SqlMapperBinderImplV2;
import mq_mapper.domain.policy.prev.SqlMapperBinderImpl;
import mq_mapper.infra.repo.EntityMetaRegistry;
import mq_mapper.infra.repo.EntityMetaRegistryImpl;

import java.util.Map;


public class AppConfig {

    private static SqlDialect sqlDialect;


    public static String MAPPER_NAME_SPACE = "dev.sj.jqm.mapper.";

    public static final EntityMetaRegistry entityMetaRegistry = new EntityMetaRegistryImpl();
    public static final SqlMapperBinder sqlMapperBinder = new SqlMapperBinderImplV2(entityMetaRegistry);



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

    public static EntityMetaRegistry getEntityMetaRegistry() {
        return entityMetaRegistry;
    }
    public static SqlMapperBinder getSqlMapperBinder() {
        return sqlMapperBinder;
    }

    public  static SqlDialect getSqlDialectImpl()
    {

        return sqlDialect;
    }





}
