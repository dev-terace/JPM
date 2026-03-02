package io.jpm.core.m_entity.generator.infra;

import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_data_source_registry.JpmDataSourceRegistry;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class MyBatisDDLDirectExecutorV2 {

    /**
     * @param nodeName 실행할 DB 노드 이름 (예: "main")
     * @param sql 실행할 DDL 문장
     */
    public void execute(String nodeName, String sql) {
        // 1. 'main' 노드 강제 체크
        if (!"main".equalsIgnoreCase(nodeName)) {
            LogPrinter.info("⚠️ [SKIP] DDL execution is restricted to the 'main' node only. Target was: " + nodeName);
            return;
        }

        // 2. 레지스트리에서 공장(SqlSessionFactory) 가져오기
        SqlSessionFactory factory = JpmDataSourceRegistry.get(nodeName);

        if (factory == null) {
            LogPrinter.error("❌ [MyBatis] No SqlSessionFactory registered for node: '" + nodeName + "'. Skipping DDL.");
            return;
        }

        // 3. 실행
        runSql(factory, sql);
    }

    private void runSql(SqlSessionFactory factory, String sql) {
        try (SqlSession session = factory.openSession(true)) { // auto-commit 활성화
            Configuration config = factory.getConfiguration();
            String statementId = "JpmDirectDDL";

            // 동적으로 MappedStatement 등록 (기존 방식 유지)
            if (!config.hasStatement(statementId)) {
                StaticSqlSource sqlSource = new StaticSqlSource(config, sql);
                MappedStatement ms = new MappedStatement.Builder(config, statementId, sqlSource, SqlCommandType.UPDATE).build();
                config.addMappedStatement(ms);
            }

            session.update(statementId);
            LogPrinter.info("✅ [MyBatis] DDL executed successfully on 'main' node.");
        } catch (Exception e) {
            LogPrinter.error("❌ [MyBatis] DDL execution failed: " + e.getMessage());

        }
    }
}