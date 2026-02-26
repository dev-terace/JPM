package jpm_repository.parse.domain.policy.sql_node_parser.node;

import config.AppConfig;
import jpm_repository.parse.domain.policy.sql_mapper_binder.SqlMapperBinder;
import jpm_repository.parse.domain.vo.DslStatement;
import jpm_repository.parse.domain.vo.BuildContext;

import jpm_repository.parse.domain.vo.EntityMeta;

import java.util.List;

public class FromSubQueryNode implements SqlNode {
    private final List<DslStatement> subStatements;
    private final EntityMeta entityMeta;
    private final String alias;
    private final SqlMapperBinder subBinder = AppConfig.getSqlMapperBinder();

    public FromSubQueryNode(DslStatement stmt, List<DslStatement> subStatements, EntityMeta entityMeta) {
        this.subStatements = subStatements;
        this.entityMeta = entityMeta;

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
        String innerSql = subBinder.generateSqlFromStatements(subStatements, entityMeta);

        if (innerSql == null || innerSql.isEmpty()) return "";

        return "(" + innerSql + ") AS " + alias;
    }
}