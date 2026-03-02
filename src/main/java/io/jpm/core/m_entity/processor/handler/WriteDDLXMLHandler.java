package io.jpm.core.m_entity.processor.handler;

import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.AstHandler;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.core.m_entity.generator.domain.policy.ddl_script_builder.DDLScriptBuilder;
import io.jpm.core.m_entity.generator.domain.policy.writer.DDLWriter;
import io.jpm.core.m_entity.generator.domain.policy.writer.MyBatisDDLXmlWriter;
import io.jpm.core.m_entity.generator.domain.vo.DDLTableMetadata;
import io.jpm.core.m_entity.processor.handler.handlerContext.DDLHandlerContext;

import javax.annotation.processing.RoundEnvironment;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WriteDDLXMLHandler extends AstHandler<DDLHandlerContext> {
    private final Map<String, String> options;
    private final DDLWriter writer;





    public WriteDDLXMLHandler(BuildTimeMetadataCache cache, GlobalRegistry globalRegistry, AstContext astContext) {
        super(cache, globalRegistry, astContext);
        this.options = globalRegistry.getOptions();
        this.writer = new MyBatisDDLXmlWriter(astContext.getFiler(), "m_ddl_generator.ddl.AutoDDL");
    }

    @Override
    public void handle(RoundEnvironment roundEnv) throws IOException {


        List<DDLTableMetadata> tables = handlerContext.getTables();
        if (tables.isEmpty()) return;
        // 1-2. SQL 생성
        String finalSql = buildSql(tables);
        // 1-3. XML 파일 기록
        writer.write(finalSql);

        handlerContext.setFinalSql(finalSql);

        LogPrinter.info(finalSql);
    }

    @Override
    public void setHandlerContext(DDLHandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }


    private String buildSql(List<DDLTableMetadata> tables) {

        return new DDLScriptBuilder(options).build(tables);
    }
}
