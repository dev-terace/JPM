package io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.node;

import io.jpm.core.jpm_repository.steps.write_dml_handler.composite.SqlMapBinder;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;

import java.util.List;

public class ExistsNode implements SqlNode {
    private final String cmd; // "whereExistsGroup" 등
    private final List<DslStatement> subStatements;
    private final EntityMeta entityMeta;

    private final RepoMetaRegistry repoMetaRegistry;
    private final SqlMapBinder subBinder;

    public ExistsNode(String cmd, List<DslStatement> subStatements, EntityMeta entityMeta, RepoMetaRegistry repoMetaRegistry, SqlMapBinder subBinder) {
        this.cmd = cmd;
        this.subStatements = subStatements;
        this.entityMeta = entityMeta;
        this.repoMetaRegistry = repoMetaRegistry;
        this.subBinder = subBinder;
    }

    @Override
    public String toSql(BuildContext ctx) {
        if (subStatements == null || subStatements.isEmpty()) {
            return (cmd.contains("Not") ? "NOT EXISTS" : "EXISTS") + " (SELECT 1)";
        }

        // 서브쿼리 내부 from()에서 실제 EntityMeta 추출
        EntityMeta subMeta = entityMeta; // 기본값
        for (DslStatement stmt : subStatements) {
            if ("from".equals(stmt.getCommand()) && !stmt.getArgs().isEmpty()) {
                String rawClass = stmt.getArgs().get(0)
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

        String subSql = subBinder.generateSqlFromStatements(subStatements, subMeta);

        String operator = cmd.contains("Not") ? "NOT EXISTS" : "EXISTS";
        return operator + " (\n" + subSql + "\n)";
    }

    @Override
    public void apply(BuildContext ctx) {
        // WhereClauseNode 내부에서 toSql을 호출해 사용할 예정
    }


}
