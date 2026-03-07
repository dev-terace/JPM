package io.jpm.core.m_entity.processor.handler;

import io.jpm.config.AutoDDLPolicy;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.AstHandler;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.GlobalRegistry;

import io.jpm.core.m_entity.generator.infra.MyBatisDDLDirectExecutorV2;
import io.jpm.core.m_entity.processor.handler.handlerContext.DDLHandlerContext;

import javax.annotation.processing.RoundEnvironment;
import java.util.Map;

public class ExecuteDDLXMLHandler extends AstHandler<DDLHandlerContext> {

    private final GlobalRegistry globalRegistry;
    private final MyBatisDDLDirectExecutorV2 ddlExecutor;
    public ExecuteDDLXMLHandler(BuildTimeMetadataCache cache, GlobalRegistry globalRegistry, AstContext astContext) {
        super(cache, globalRegistry, astContext);
        this.globalRegistry = globalRegistry;
        this.ddlExecutor = new MyBatisDDLDirectExecutorV2();
    }

    @Override
    public void handle(RoundEnvironment roundEnv) throws Exception {

        String finalSql = handlerContext.getFinalSql();

        String cleanedSql = finalSql
                .replace("<![CDATA[", "")  // 시작 태그 삭제
                .replace("]]>", "");        // 끝 태그 삭제

        // 1-4. DB 연결 옵션 가져오기
        Map<String, String> options = globalRegistry.options();

        // 1-5. 즉시 DDL 실행 (실패 시 여기서 중단됨)
        String auto = options.get("auto");
        boolean isCreateExec = AutoDDLPolicy.CREATE_N_EXE.name().equals(auto);
        boolean isDropExec   = AutoDDLPolicy.DROP_N_CREATE_EXE.name().equals(auto);
        boolean isAlterExec  = AutoDDLPolicy.ALTER_N_EXE.name().equals(auto);


        // 3. 하나라도 해당되면 '실행해야 하는 상태'로 판단
        boolean shouldExecute = isCreateExec || isDropExec || isAlterExec;

        if(shouldExecute)
        {
            ddlExecutor
                    .execute("main", cleanedSql);
        }
    }

    @Override
    public void setHandlerContext(DDLHandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }


}
