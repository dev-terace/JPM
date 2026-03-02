package io.jpm.core.jpm_data_source_registry;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class JpmDataSourceRegistry {
    private static final Map<String, SqlSessionFactory> registry = new ConcurrentHashMap<>();

    public static void load(Properties props) {
        String nodes = props.getProperty("jpm.db.nodes");
        if (nodes == null) return;

        for (String node : nodes.split(",")) {
            String name = node.trim();
            String prefix = "jpm.db." + name + ".";

            // 1. 해당 노드의 설정값 읽기
            String url = props.getProperty(prefix + "url");
            String user = props.getProperty(prefix + "username");
            String pass = props.getProperty(prefix + "password");
            String driver = props.getProperty(prefix + "driver");

            if (url != null) {
                // 2. SqlSessionFactory 빌드 및 등록
                registry.put(name, createFactory(url, user, pass, driver));
                System.out.println("🚀 [JPM-BUILD] DB 등록 완료: " + name);
            }
        }
    }

    private static SqlSessionFactory createFactory(String url, String user, String pass, String driver) {
        PooledDataSource ds = new PooledDataSource();
        ds.setDriver(driver);
        ds.setUrl(url);
        ds.setUsername(user);
        ds.setPassword(pass);

        Environment env = new Environment("jpm-" + url, new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(env);
        return new SqlSessionFactoryBuilder().build(config);
    }

    public static SqlSessionFactory get(String name) {
        return registry.get(name);
    }
}