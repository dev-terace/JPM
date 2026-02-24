package mq_repository.infra;



import config.AppConfig;
import mq_mapper.domain.vo.DslStatement;
import mq_mapper.infra.repo.EntityMetaRegistry;
import mq_mapper.infra.repo.EntityMetaRegistryImpl;
import mq_mapper.infra.SqlMapperBinder;
import mq_repository.domain.SqlNode;

import mq_mapper.domain.vo.EntityMeta;

import java.util.List;

public class JoinGroupNode implements SqlNode {
    private final String joinType;
    private final String targetClass;   // 예: "OrderItemEntity.class"
    private final String leftCol;       // 예: "orders.id"
    private final String rightCol;      // 예: "item_summary.order_id"
    private final List<DslStatement> subStatements; // 👈 이 변수명으로 통일
    private final EntityMeta mainEntityMeta;


    private static final EntityMetaRegistry entityMetaRegistry = AppConfig.getEntityMetaRegistry();

    public JoinGroupNode(String cmd, List<String> args, List<DslStatement> subStatements, EntityMeta entityMeta) {
        this.joinType = cmd.startsWith("left") ? "LEFT JOIN" : "INNER JOIN";
        // args: [targetClass, leftCol, rightCol]
        this.targetClass = (!args.isEmpty()) ? args.get(0) : "";
        this.leftCol = (args.size() > 1) ? args.get(1) : "";
        this.rightCol = (args.size() > 2) ? args.get(2) : "";
        this.subStatements = subStatements; // 생성자 주입
        this.mainEntityMeta = entityMeta;
    }

    @Override
    public void apply(SqlMapperBinder.BuildContext ctx) {
        String joinSql = toSql(ctx);
        if (!joinSql.isEmpty()) {
            ctx.joins.add(joinSql);
        }
    }

    @Override
    public String toSql(SqlMapperBinder.BuildContext ctx) {
        // 1. 진짜 별칭(Alias) 추출: "item_summary.order_id" -> "item_summary"
        String realAlias = "sub_query";
        if (rightCol.contains(".")) {
            realAlias = rightCol.split("\\.")[0];
        } else if (rightCol.contains("|")) {
            realAlias = rightCol.split("\\|")[0];
        }

        // 2. 서브쿼리용 메타데이터 결정
        // Join 대상인 OrderItemEntity.class의 메타를 가져와야 서브쿼리 내부 컬럼명이 정확히 변환됩니다.
        String cleanedClass = targetClass.replace(".class", "").replace("class ", "");
        EntityMeta subMeta = entityMetaRegistry.getEntityMeta(cleanedClass);

        // 서브쿼리 전용 바인더 생성 및 실행
        SqlMapperBinder subBinder = new SqlMapperBinder();
        // 🚀 여기서 this.subStatements를 사용합니다!
        String subQuerySql = subBinder.generateSqlFromStatements(this.subStatements, subMeta != null ? subMeta : mainEntityMeta);

        // 3. ON 조건 정제
        String resolvedLeft = leftCol.replace("|", ".");
        String resolvedRight = rightCol.replace("|", ".");

        // 최종 SQL 조립
        return String.format("%s (\n%s\n) AS %s ON %s = %s",
                joinType,
                indent(subQuerySql),
                realAlias,
                resolvedLeft,
                resolvedRight);
    }

    private String indent(String sql) {
        if (sql == null || sql.isEmpty()) return "";
        return "    " + sql.replace("\n", "\n    ");
    }
}
