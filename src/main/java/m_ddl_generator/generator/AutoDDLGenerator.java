package m_ddl_generator.generator;

import auto_ddl.AutoDDLPolicy;
import m_ddl_generator.model.TableMetadata;
import m_ddl_generator.parser.MetadataLoader;
import m_ddl_generator.writer.DdlWriter;

import utils.JpmOptionsLoader;


import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

import java.io.IOException;
;
import java.util.List;
import java.util.Map;


public class AutoDDLGenerator {
    private final MetadataLoader loader;
    private final DdlWriter writer;
    private final ProcessingEnvironment processingEnv;
    private final Map<String, String> options;
    ExecutorSourceWriter executorWriter;
    // 상수 정의
    private static final String AUTO_EXECUTOR_PACKAGE = "m_ddl_generator.executor";
    private static final String EXECUTOR_CLASS_NAME = "JpmAutoSQLExecutor";

    public static class GeneratorCommand {
        public String sql;
        public String url;
        public String username;
        public String password;
        public String dbType;
        public String sqlCommandType;
    }

    public AutoDDLGenerator(MetadataLoader loader,
                            DdlWriter writer,
                            ProcessingEnvironment processingEnv,
                            ExecutorSourceWriter executorWriter,
                            Map<String, String> options) { // 👈 파라미터 추가
        this.loader = loader;

        this.writer = writer;
        this.processingEnv = processingEnv;
        this.executorWriter = executorWriter;
        this.options = options; // 👈 저장
    }

    // ===================================================================================
    // 1. Main Entry Point
    // ===================================================================================
    public void generate() {
        try {
            // 1-1. 메타데이터 로드
            List<TableMetadata> tables = loader.load(null);
            if (tables.isEmpty()) return;
            // 1-2. SQL 생성
            String finalSql = buildSql(tables);
            // 1-3. XML 파일 기록
            writer.write(finalSql);

            String cleanedSql = finalSql
                    .replace("<![CDATA[", "")  // 시작 태그 삭제
                    .replace("]]>", "");        // 끝 태그 삭제

            // 1-4. DB 연결 옵션 가져오기
            Map<String, String> options = JpmOptionsLoader.loadOptions(processingEnv);
            validateOptions(options);
            // 1-5. 즉시 DDL 실행 (실패 시 여기서 중단됨)
            String auto = options.get("auto");
            boolean isCreateExec = AutoDDLPolicy.CREATE_N_EXE.name().equals(auto);
            boolean isDropExec   = AutoDDLPolicy.DROP_N_CREATE_EXE.name().equals(auto);
            boolean isAlterExec  = AutoDDLPolicy.ALTER_N_EXE.name().equals(auto);


            // 3. 하나라도 해당되면 '실행해야 하는 상태'로 판단
            boolean shouldExecute = isCreateExec || isDropExec || isAlterExec;

            if(shouldExecute)
            {
                new MyBatisDirectExecutor(processingEnv.getMessager())
                        .execute(cleanedSql, options);
            }
            // 1-6. Executor 소스 코드 생성

            GeneratorCommand cmd = createCommand(cleanedSql, options);

            generateExecutorSource(cmd);

        } catch (Exception e) {
            logError("AutoDDL Generation Error: " + e.getMessage());
        }
    }

    // ===================================================================================
    // 2. Helper Methods for Logic
    // ===================================================================================
    private String buildSql(List<TableMetadata> tables) {

        return new DdlScriptBuilder(options).build(tables);
    }

    private void validateOptions(Map<String, String> options) {
        if (options.get("url") == null || options.get("username") == null) {
            throw new RuntimeException("DB Connection options (url, username) are missing.");
        }
    }


    private GeneratorCommand createCommand(String sql, Map<String, String> options) {
        GeneratorCommand cmd = new GeneratorCommand();
        cmd.sql = sql;
        cmd.url = options.get("url");
        cmd.username = options.get("username");
        cmd.password = options.get("password");
        cmd.dbType = options.getOrDefault("dbType", "POSTGRES");
        cmd.sqlCommandType = options.getOrDefault("sqlType", "UPDATE");
        return cmd;
    }







    // ===================================================================================
    // 3. Source Code Generation (Writer)
    // ===================================================================================
    private void generateExecutorSource(GeneratorCommand cmd) throws IOException {
        try {
            ExecutorSourceWriter execute = new JpmExecutorSourceWriter(processingEnv);
            execute.write(AUTO_EXECUTOR_PACKAGE, EXECUTOR_CLASS_NAME, cmd);
        } catch (Exception e) {
            logError("Executor 소스 생성 실패: " + e.getMessage());
        }
    }


    private void logError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }
}