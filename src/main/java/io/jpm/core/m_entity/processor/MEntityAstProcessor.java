package io.jpm.core.m_entity.processor;

import io.jpm.common.exception.config.JpmFieldExtractorScanner;
import io.jpm.common.exception.config.JpmToolbox;
import io.jpm.config.AutoDDLPolicy;
import io.jpm.core.m_entity.generator.domain.policy.ddl_script_builder.DDLScriptBuilder;
import io.jpm.core.m_entity.generator.domain.policy.my_batis_ddl_executor_source_write.JpmMyBatisXMLDDLExecutorSourceWriter;
import io.jpm.core.m_entity.generator.domain.policy.writer.MyBatisDDLXmlWriter;
import io.jpm.core.m_entity.generator.infra.MyBatisDDLDirectExecutor;
import io.jpm.core.m_entity.generator.domain.policy.my_batis_ddl_executor_source_write.MyBatisXMLDDLExecutorSourceWriter;
import io.jpm.core.m_entity.generator.domain.vo.DDLTableMetadata;
import io.jpm.core.m_entity.parse.domain.ast.DDLMetaDataLoader;
import io.jpm.core.m_entity.generator.domain.policy.writer.DDLWriter;

import io.jpm.core.m_entity.parse.infra.ast.DDLMetaDataLoaderAstImplV2;
import io.jpm.common.utils.JpmOptionsLoader;
import io.jpm.common.utils.LogPrinter;


import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic;

import java.io.IOException;
;
import java.util.List;
import java.util.Map;


@Deprecated
public class MEntityAstProcessor {
    private final DDLMetaDataLoader loader;
    private final DDLWriter writer;
    private final ProcessingEnvironment processingEnv;
    private final Map<String, String> options;

    // 상수 정의
    private static final String AUTO_EXECUTOR_PACKAGE = "m_ddl_generator.executor";
    private static final String EXECUTOR_CLASS_NAME = "JpmAutoSQLExecutor";
    private final JpmFieldExtractorScanner fieldExtractorScanner;
    public static class GeneratorCommand {
        public String sql;
        public String url;
        public String username;
        public String password;
        public String dbType;
        public String sqlCommandType;
    }

    public MEntityAstProcessor(
                               ProcessingEnvironment processingEnv,
                               RoundEnvironment roundEnv,
                               Map<String, String> options) { // 👈 파라미터 추가
        this.loader = new DDLMetaDataLoaderAstImplV2(processingEnv, roundEnv, null);

        this.writer = new MyBatisDDLXmlWriter(processingEnv.getFiler(), "m_ddl_generator.ddl.AutoDDL");
        this.processingEnv = processingEnv;
        this.fieldExtractorScanner = new JpmFieldExtractorScanner(null, new JpmToolbox(processingEnv));
        this.options = options; // 👈 저장
    }

    // ===================================================================================
    // 1. Main Entry Point
    // ===================================================================================
    public void generate() {
        try {
            // 1-1. 메타데이터 로드

            List<DDLTableMetadata> tables = loader.load(null);


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
                new MyBatisDDLDirectExecutor()
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
    private String buildSql(List<DDLTableMetadata> tables) {

        return new DDLScriptBuilder(options).build(tables);
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
            MyBatisXMLDDLExecutorSourceWriter execute = new JpmMyBatisXMLDDLExecutorSourceWriter(processingEnv);
            execute.write(AUTO_EXECUTOR_PACKAGE, EXECUTOR_CLASS_NAME, cmd);
        } catch (Exception e) {
            logError("Executor 소스 생성 실패: " + e.getMessage());
        }
    }


    private void logError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }
}