package mq_mapper.domain.policy;

import mq_mapper.domain.vo.DslStatement;
import mq_mapper.domain.vo.EntityMeta;
import mq_mapper.domain.vo.MethodMeta;
import mq_mapper.infra.repo.EntityMetaRegistry;
import mq_repository.domain.BuildContext;
import mq_repository.domain.SqlNode;
import utils.LogPrinter;

import java.util.List;

/**
 * DSL Statement 목록을 순회하여 SQL 문자열을 생성하는 바인더.
 *
 * <p>이 클래스는 각 단계를 조율하는 역할만 담당합니다.
 *
 * <ul>
 *   <li>{@link AliasPreScanner}  – JOIN/FROM 선언을 미리 읽어 테이블 별칭 맵 구성</li>
 *   <li>{@link SqlNodeParser}    – Statement → Node 트리 변환</li>
 *   <li>{@link SqlAssembler}     – 수집된 절을 SQL 문자열로 조립</li>
 * </ul>
 */

public class SqlMapperBinderImplV2 implements SqlMapperBinder {

    private final AliasPreScanner aliasPreScanner;
    private final SqlNodeParser   sqlNodeParser;

    // ── 생성자 (의존성 주입) ──────────────────────────────────────────

    public SqlMapperBinderImplV2(EntityMetaRegistry entityMetaRegistry) {
        ArgResolver argResolver  = new ArgResolver(entityMetaRegistry);
        this.aliasPreScanner     = new AliasPreScanner(entityMetaRegistry);
        this.sqlNodeParser       = new SqlNodeParser(entityMetaRegistry, argResolver);
    }

    /** AppConfig 기반 기본 인스턴스 생성 팩토리 메서드 */


    // ── 공개 API ─────────────────────────────────────────────────────

    @Override
    public String generateSql(MethodMeta method, EntityMeta entityMeta) {
        return generateSqlFromStatements(method.getStatements(), entityMeta);
    }

    @Override
    public String generateSqlFromStatements(List<DslStatement> statements, EntityMeta entityMeta) {
        try {
            BuildContext ctx = new BuildContext(entityMeta);

            // 1. 별칭 사전 스캔
            aliasPreScanner.scan(statements, ctx);

            // 2. Statement → Node 변환
            List<SqlNode> nodes = sqlNodeParser.parse(statements, ctx, entityMeta);

            // 3. Node 실행 (BuildContext에 데이터 적재)
            for (SqlNode node : nodes) {
                LogPrinter.info("[node]: " + node.getClass().getName());
                node.apply(ctx);
            }

            // 4. SQL 조립
            return SqlAssembler.assemble(ctx);

        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }
}