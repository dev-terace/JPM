package io.jpm.core.jpm_repository.runtime.core.node;

import io.jpm.api.TerraceQuery;
import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;

import io.jpm.core.jpm_repository.domain.model.DslStatementV2;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.runtime.core.DslStatementArgResolver;
import io.jpm.core.jpm_repository.runtime.core.SqlNodeParserStepsV2;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node.SqlNode;

import java.util.List;
import java.util.stream.Collectors;

public class ExistsNodeV2 implements SqlNode {
    private final String cmd; // "whereExistsGroup" 등
    private final List<DslStatementV2> subStatements;
    private final EntityMeta entityMeta;

    private final RepoMetaRegistry repoMetaRegistry;
    private final SqlNodeParserStepsV2 nodeParser;

    

    public ExistsNodeV2(String cmd, List<DslStatementV2> subStatements, EntityMeta entityMeta, RepoMetaRegistry repoMetaRegistry, SqlNodeParserStepsV2 nodeParser) {
        this.cmd = cmd;
        this.subStatements = subStatements;
        this.entityMeta = entityMeta;
        this.repoMetaRegistry = repoMetaRegistry;
        this.nodeParser = nodeParser;
    }

    @Override
    public String toSql(BuildContext ctx) {
        if (subStatements == null || subStatements.isEmpty()) {
            return (cmd.contains("Not") ? "NOT EXISTS" : "EXISTS") + " (SELECT 1)";
        }

        // 서브쿼리 내부 from()에서 실제 EntityMeta 추출
        EntityMeta subMeta = entityMeta; // 기본값
        for (DslStatementV2 stmt : subStatements) {
            if ("from".equals(stmt.getCommand()) && !stmt.getArgs().isEmpty()) {
                List<String> resolveStmt = DslStatementArgResolver.resolve(stmt);


                String rawClass = resolveStmt.get(0)
                        .replace(".class", "")
                        .replaceAll("^class .*\\.", ""); // 패키지명 제거

                EntityMeta found = repoMetaRegistry.getEntityMeta(rawClass);
                if (found != null) {
                    subMeta = found;
                    break;
                }
            }
        }

        // 서브쿼리용 독립 컨텍스트 (부모 별칭 맵 상속)

        String subSql = nodeParser.generateSqlFromStatements(subStatements);
        LogPrinter.info("[ExistsNode] " + subMeta);
        String operator = cmd.contains("Not") ? "NOT EXISTS" : "EXISTS";
        return operator + " (\n" + subSql + "\n)";
    }

    @Override
    public void apply(BuildContext ctx) {
        // WhereClauseNode 내부에서 toSql을 호출해 사용할 예정
    }


}
