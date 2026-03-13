package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node;


import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.BuildContext;

import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.SqlNodeParserSteps;

import java.util.List;

public class FromSubQueryNode implements SqlNode {
    private final List<DslStatement> subStatements;
    private final EntityMeta entityMeta;
    private final String alias;
    private final SqlNodeParserSteps nodeParser;

    public FromSubQueryNode(DslStatement stmt, List<DslStatement> subStatements, EntityMeta entityMeta, SqlNodeParserSteps nodeParser) {
        this.subStatements = subStatements;
        this.entityMeta = entityMeta;
        this.nodeParser = nodeParser;

        // 🚀 수정한 부분: List<Object>를 List<String>으로 변경
        List<String> args = stmt.getArgs();
        this.alias = (args != null && !args.isEmpty()) ? args.get(0) : "sub";
    }

    @Override
    public void apply(BuildContext ctx) {
        String subQuerySql = toSql(ctx);
        if (subQuerySql != null && !subQuerySql.isEmpty()) {
            ctx.getTables().add(subQuerySql);
            // 서브쿼리 별칭 등록
            ctx.getTableAliases().put(this.alias, "SUBQUERY");
        }
    }

    @Override
    public String toSql(BuildContext ctx) {

        // 앞서 추가한 generateSqlFromStatements 메서드 호출
        String innerSql = nodeParser.generateSqlFromStatements(subStatements, entityMeta);

        if (innerSql == null || innerSql.isEmpty()) return "";

        return "(" + innerSql + ") AS " + alias;
    }
}