package io.jpm.core.jpm_repository.steps.write_dml_handler.composite;

import io.jpm.common.exception.CustomProcessorException;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;

import io.jpm.core.jpm_repository.steps.write_dml_handler.context.SqlMapBinderContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.core.alias_pre_scanner.AliasPreScannerStep;
import io.jpm.core.jpm_repository.steps.write_dml_handler.utils.SqlAssemblerUtil;

import io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.ArgResolver;
import io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.SqlNodeParserSteps;
import io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.node.SqlNode;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.List;

public class CompositeSqlMapperBinderStep implements Step<SqlMapBinderContext>, SqlMapBinder {

    private final AliasPreScannerStep aliasPreScanner;
    private final SqlNodeParserSteps sqlNodeParser;

    public CompositeSqlMapperBinderStep(RepoMetaRegistry repoMetaRegistry, ColumnResolver columnResolver) {

        ArgResolver argResolver  = new ArgResolver(repoMetaRegistry);
        this.aliasPreScanner     = new AliasPreScannerStep(repoMetaRegistry);
        this.sqlNodeParser       = new SqlNodeParserSteps(repoMetaRegistry, argResolver, columnResolver, this);

    }


    @Override
    public void execute(SqlMapBinderContext context) throws Exception {
        String finalSql = generateSqlFromStatements(context.getMethod().getStatements(), context.getEntityMeta());

        context.setFinalSql(finalSql);
    }



    public String generateSqlFromStatements(List<DslStatement> statements, EntityMeta entityMeta) {
        try {
            BuildContext ctx = new BuildContext(entityMeta);

            // 1. 별칭 사전 스캔

            aliasPreScanner.scan(statements, ctx);

            // 2. Statement → Node 변환
            List<SqlNode> nodes = sqlNodeParser.parse(statements, ctx, entityMeta);

            // 3. Node 실행 (BuildContext에 데이터 적재)
            for (SqlNode node : nodes) {
                node.apply(ctx);

            }

            // 4. SQL 조립
            return SqlAssemblerUtil.assemble(ctx);

        } catch (CustomProcessorException e) {
            throw new CustomProcessorException(e.getErrorCode());
        }

        catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }


}
