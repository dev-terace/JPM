package m_ddl_generator.generator;

import m_ddl_generator.dialect.PostgreSqlDialect;
import m_ddl_generator.dialect.SqlDialect;
import m_ddl_generator.model.ColumnMetadata;
import m_ddl_generator.model.TableMetadata;
import m_ddl_generator.parser.AnnotationMetadataLoader;
import m_ddl_generator.parser.MetadataLoader;
import m_ddl_generator.writer.DdlWriter;
import m_ddl_generator.writer.MyBatisXmlWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.ArrayList;
import java.util.List;

public class AutoDDLGenerator {

    private final MetadataLoader loader;
    private final SqlDialect dialect;
    private final DdlWriter writer;

    // 생성자 주입 (Dependency Injection)
    public AutoDDLGenerator(MetadataLoader loader, SqlDialect dialect, DdlWriter writer) {
        this.loader = loader;
        this.dialect = dialect;
        this.writer = writer;
    }

    // 팩토리 메서드 (편의용)
    public static AutoDDLGenerator createDefault(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, String dbType) {
        // 1. DB 타입에 따른 Dialect 선택
        SqlDialect dialect;
        if (dbType != null && dbType.toUpperCase().contains("POSTGRES")) {
            dialect = new PostgreSqlDialect();
        } else {
            // dialect = new MySqlDialect(); // 구현 필요 시 추가
            throw new IllegalArgumentException("Unsupported DB Type: " + dbType);
        }

        // 2. Loader 및 Writer 생성
        MetadataLoader loader = new AnnotationMetadataLoader(processingEnv, roundEnv);
        DdlWriter writer = new MyBatisXmlWriter(processingEnv.getFiler(), "dev.sj.jqm.mapper.ddl");

        return new AutoDDLGenerator(loader, dialect, writer);
    }

    public void generate() {
        try {
            // 1. 메타데이터 로드
            List<TableMetadata> tables = loader.load(null); // RoundEnv는 생성자에서 처리했으므로
            if (tables.isEmpty()) return;

            StringBuilder sqlBuffer = new StringBuilder();
            List<String> alterSqls = new ArrayList<>();

            // 2. 테이블 생성 SQL 빌드
            for (TableMetadata table : tables) {
                // DROP 문
                for (String sql : dialect.createDropTableSql(table)) {
                    sqlBuffer.append("        ").append(sql).append("\n");
                }
                // CREATE 문
                sqlBuffer.append("        ").append(dialect.createTableDefinitionSql(table)).append("\n");

                // ALTER 문 (FK 등) 수집
                for (ColumnMetadata col : table.getColumns()) {
                    alterSqls.addAll(dialect.createAlterTableSql(table, col));
                }
            }

            // 3. 수집된 ALTER 문 추가
            if (!alterSqls.isEmpty()) {
                sqlBuffer.append("\n");
                for (String sql : alterSqls) {
                    sqlBuffer.append("        ").append(sql).append("\n");
                }
            }

            // 4. 파일 쓰기
            writer.write(sqlBuffer.toString());
            System.out.println("✅ DDL Generated Successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("DDL Generation Failed", e);
        }
    }
}