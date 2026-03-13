package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser;

import io.jpm.common.exception.CustomProcessorException;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.composite.CompositeSqlMapperBinderStep;

import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context.AliasScanContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context.SqlMapBinderContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context.SqlNodeParserContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.alias_pre_scanner.AliasPreScannerSteps;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.BuildSqlNodesStep;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.ArgResolver;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node.SqlNode;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.utils.SqlAssemblerUtil;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.List;

public class SqlNodeParserSteps implements Step<SqlMapBinderContext> {
    private final DetectJoinPrefixStep detectJoinPrefixStep = new DetectJoinPrefixStep();
    private final BuildSqlNodesStep buildSqlNodesStep;
    private final AliasPreScannerSteps aliasPreScannerSteps;
    private final RepoMetaRegistry repoMetaRegistry;
    public SqlNodeParserSteps(RepoMetaRegistry repoMetaRegistry,
                         ArgResolver argResolver,
                         ColumnResolver columnResolver,
                         CompositeSqlMapperBinderStep binder) {

        this.repoMetaRegistry = repoMetaRegistry;
        aliasPreScannerSteps = new AliasPreScannerSteps(repoMetaRegistry);
        this.buildSqlNodesStep = new BuildSqlNodesStep(repoMetaRegistry, argResolver, columnResolver, this);
    }

    @Override
    public void execute(SqlMapBinderContext ctx) throws Exception {
        String finalSql = buildSql(ctx.getMethod().getStatements(), ctx.getBuildContext(), ctx.getEntityMeta());
        ctx.setFinalSql(finalSql);
    }

    // ② SQL 문자열 직접 반환
    public String generateSqlFromStatements(List<DslStatement> statements, EntityMeta entityMeta) {
        BuildContext buildCtx = new BuildContext(entityMeta);
        return buildSql(statements, buildCtx, entityMeta);
    }




    private String buildSql(List<DslStatement> statements, BuildContext buildCtx, EntityMeta entityMeta) {
        try {
            // 1. 별칭 사전 스캔
            aliasPreScannerSteps.execute(
                    new AliasScanContext(statements, buildCtx, repoMetaRegistry)
            );

            // 2. Statement → Node 변환
            SqlNodeParserContext sqlNodeCtx = new SqlNodeParserContext(statements, buildCtx, entityMeta);
            detectJoinPrefixStep.execute(sqlNodeCtx);
            buildSqlNodesStep.execute(sqlNodeCtx);

            // 3. Node 실행
            for (SqlNode node : sqlNodeCtx.getNodes()) {
                node.apply(buildCtx);
            }

            // 4. SQL 조립
            return SqlAssemblerUtil.assemble(buildCtx);

        } catch (CustomProcessorException e) {
            throw new CustomProcessorException(e.getErrorCode());
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

}
