package ddl_executor;

import config.AppConfig;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources; // ★ 핵심: MyBatis 리소스 로더
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.io.IOException;
import java.io.InputStream;

public class AutoDDLExecutor {

    private final SqlSessionFactory sqlSessionFactory;

    // 1. 실행할 매퍼 정보 (네임스페이스와 ID는 XML 내용과 정확히 일치해야 함)
    private static final String NAMESPACE = AppConfig.MAPPER_NAME_SPACE; // 예: "dev.sj.mapper.ddl"
    private static final String STATEMENT_ID = "ddl.execute_auto_ddl";

    // 2. 클래스패스 상의 XML 파일 위치 (resources 폴더 기준)
    // Gradle 설정에 따라 경로가 다를 수 있지만, 패키지명과 동일하게 폴더가 생성된다면 아래와 같이 설정
    // 예: src/main/resources/ddl/ddl.xml -> "ddl/ddl.xml"
    private static final String XML_RESOURCE_PATH = "ddl/ddl.xml";

    public AutoDDLExecutor(DbConfig config) {
        this.sqlSessionFactory = createSqlSessionFactory(config);
    }

    private SqlSessionFactory createSqlSessionFactory(DbConfig config) {
        PooledDataSource dataSource = new PooledDataSource();
        dataSource.setDriver(config.getDriverClassName());
        dataSource.setUrl(config.getUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        dataSource.setPoolMaximumActiveConnections(5);

        JdbcTransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("ddl_env", transactionFactory, dataSource);

        // Configuration 생성
        Configuration configuration = new Configuration(environment);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * 이제 파일 경로를 받지 않습니다.
     * 설정된 Resource 경로에서 XML을 로드하고, Namespace로 실행합니다.
     */
    public void run() {
        System.out.println("🔧 [AutoDDL] Initializing execution...");

        try (SqlSession session = sqlSessionFactory.openSession()) {
            Configuration config = session.getConfiguration();

            // 1. XML 리소스 로드 (File Path가 아니라 Classpath Resource 사용)
            // MyBatis의 Resources 유틸리티를 사용하면 경로 문제를 해결해줍니다.
            try (InputStream inputStream = Resources.getResourceAsStream(XML_RESOURCE_PATH)) {
                if (inputStream == null) {
                    throw new RuntimeException("❌ XML Resource not found in classpath: " + XML_RESOURCE_PATH);
                }

                XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(
                        inputStream,
                        config,
                        XML_RESOURCE_PATH,
                        config.getSqlFragments()
                );
                mapperBuilder.parse(); // 메모리에 XML 등록
            }

            // 2. 실행할 SQL ID 조합 (Namespace + ID)
            String fullStatementId = NAMESPACE  + STATEMENT_ID;

            // 3. 매퍼 ID 존재 확인
            if (!config.hasStatement(fullStatementId)) {
                throw new RuntimeException("❌ Statement ID not found: " + fullStatementId);
            }

            // 4. SQL 실행
            System.out.println("🔨 [AutoDDL] Executing SQL via Namespace: " + fullStatementId);
            session.update(fullStatementId);

            // 5. 커밋
            session.commit();
            System.out.println("✅ [AutoDDL] Successfully applied to PostgreSQL.");

        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to load XML resource: " + XML_RESOURCE_PATH, e);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error executing DDL", e);
        }
    }
}