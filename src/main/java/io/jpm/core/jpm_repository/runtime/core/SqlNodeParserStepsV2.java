package io.jpm.core.jpm_repository.runtime.core;

import io.jpm.common.exception.CustomProcessorException;
import io.jpm.common.utils.CustomLogger;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.RepoRelationRegistryImpl;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;
import io.jpm.core.jpm_repository.domain.model.result_map_meta.ResultMapMeta;
import io.jpm.core.jpm_repository.runtime.config.AppConfig;
import io.jpm.core.jpm_repository.runtime.context.AliasScanContextV2;
import io.jpm.core.jpm_repository.runtime.context.SqlMapBinderContextV2;
import io.jpm.core.jpm_repository.runtime.context.SqlNodeParserContextV2;
import io.jpm.core.jpm_repository.runtime.core.alias_pre_scanner.AliasPreScannerStepsV2;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node.SqlNode;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.utils.SqlAssemblerUtil;



import java.util.List;



public class SqlNodeParserStepsV2 implements Step<SqlMapBinderContextV2> {
    private final DetectJoinPrefixStepV2 detectJoinPrefixStep = new DetectJoinPrefixStepV2();
    private final AliasPreScannerStepsV2 aliasPreScannerSteps;
    private final RepoMetaRegistry repoMetaRegistry;
    private final BuildSqlNodesStepV2 buildSqlNodesStepV2;
    private final CustomLogger log = CustomLogger.getLogger(SqlNodeParserStepsV2.class);



    public SqlNodeParserStepsV2( RepoMetaRegistry repoMetaRegistry
    ) {

        this.repoMetaRegistry = repoMetaRegistry;

        aliasPreScannerSteps = new AliasPreScannerStepsV2();
        buildSqlNodesStepV2 = new BuildSqlNodesStepV2(repoMetaRegistry, this);


    }

    @Override
    public void execute(SqlMapBinderContextV2 ctx) throws Exception {
        String finalSql = buildSql(ctx.getStatements(), ctx.getBuildContext());

        ctx.setFinalSql(finalSql);

    }



    public String generateSqlFromStatements(List<DslStatementV2> statements) {
        BuildContext buildCtx = new BuildContext();

        return buildSql(statements, buildCtx);
    }


    private String buildSql(List<DslStatementV2> statements, BuildContext buildCtx) {
        try {
            // 1. 별칭 사전 스캔
            aliasPreScannerSteps.execute(
                    new AliasScanContextV2(statements, buildCtx, repoMetaRegistry)
            );

            // 2. Statement → Node 변환
            log.debug("[statements] : {}", statements);

            SqlNodeParserContextV2 sqlNodeCtx = new SqlNodeParserContextV2(statements, buildCtx);
            detectJoinPrefixStep.execute(sqlNodeCtx);
            buildSqlNodesStepV2.execute(sqlNodeCtx);




            // 3. Node 실행
            for (SqlNode node : sqlNodeCtx.getNodes()) {
                node.apply(buildCtx);
            }

            // 4. SQL 조립

            log.debug(SqlAssemblerUtil.assemble(buildCtx));


            return SqlAssemblerUtil.assemble(buildCtx);

        } catch (CustomProcessorException e) {
            throw new CustomProcessorException(e.getErrorCode());
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

}
