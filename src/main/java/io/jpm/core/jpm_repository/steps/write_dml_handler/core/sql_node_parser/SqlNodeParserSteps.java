package io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser;

import io.jpm.common.exception.CustomProcessorException;
import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.steps.write_dml_handler.context.SqlNodeParserContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.composite.SqlMapBinder;
import io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.node.SqlNode;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.List;

public class SqlNodeParserSteps {
    private final DetectJoinPrefixStep detectJoinPrefixStep = new DetectJoinPrefixStep();
    private final BuildSqlNodesStep    buildSqlNodesStep;

    public SqlNodeParserSteps(RepoMetaRegistry repoMetaRegistry,
                         ArgResolver argResolver,
                         ColumnResolver columnResolver,
                         SqlMapBinder binder) {
        this.buildSqlNodesStep = new BuildSqlNodesStep(repoMetaRegistry, argResolver, columnResolver, binder);
    }

    public List<SqlNode> parse(List<DslStatement> statements, BuildContext buildCtx, EntityMeta entityMeta) {
        try {
            SqlNodeParserContext ctx = new SqlNodeParserContext(statements, buildCtx, entityMeta);

            detectJoinPrefixStep.execute(ctx);
            buildSqlNodesStep.execute(ctx);

            return ctx.getNodes();

        } catch (CustomProcessorException e) {
            throw new CustomProcessorException(e.getErrorCode());
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }
}
