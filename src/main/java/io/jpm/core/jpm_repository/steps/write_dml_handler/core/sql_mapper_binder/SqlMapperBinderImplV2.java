package io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_mapper_binder;

import io.jpm.common.exception.CustomProcessorException;
import io.jpm.core.jpm_repository.utils.ColumnResolver;
import io.jpm.core.jpm_repository.steps.write_dml_handler.core.alias_pre_scanner.AliasPreScanner;
import io.jpm.core.jpm_repository.steps.write_dml_handler.utils.SqlAssemblerUtil;
import io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.ArgResolver;
import io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.SqlNodeParser;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.node.SqlNode;
import io.jpm.common.utils.LogPrinter;

import java.util.List;

/**
 * DSL Statement 목록을 순회하여 SQL 문자열을 생성하는 바인더.
 *
 * <p>이 클래스는 각 단계를 조율하는 역할만 담당합니다.
 *
 * <ul>
 *   <li>{@link AliasPreScanner}  – JOIN/FROM 선언을 미리 읽어 테이블 별칭 맵 구성</li>
 *   <li>{@link SqlNodeParser}    – Statement → Node 트리 변환</li>
 *   <li>{@link SqlAssemblerUtil}     – 수집된 절을 SQL 문자열로 조립</li>
 * </ul>
 */

@Deprecated
public class SqlMapperBinderImplV2 implements SqlMapperBinder {

    private final AliasPreScanner aliasPreScanner;
    private final SqlNodeParser   sqlNodeParser;

    // ── 생성자 (의존성 주입) ──────────────────────────────────────────

    public SqlMapperBinderImplV2(RepoMetaRegistry repoMetaRegistry, ColumnResolver columnResolver) {
        ArgResolver argResolver  = new ArgResolver(repoMetaRegistry);
        this.aliasPreScanner     = new AliasPreScanner(repoMetaRegistry);
        this.sqlNodeParser       = new SqlNodeParser(repoMetaRegistry, argResolver, columnResolver, this);
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