package io.jpm.config;


import io.jpm.core.m_entity.dialect.MySqlDialect;
import io.jpm.core.m_entity.dialect.PostgreSqlDialect;
import io.jpm.core.m_entity.dialect.SqlDialect;



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
