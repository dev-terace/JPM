package io.jpm.core.jpm_repository.parse.domain.policy.sql_node_parser.node;

import io.jpm.config.AppConfig;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;

import io.jpm.core.jpm_repository.parse.domain.cache.EntityRelationRegistry;
import io.jpm.core.jpm_repository.parse.domain.vo.BuildContext;
import io.jpm.core.jpm_repository.parse.domain.vo.EntityMeta;
import io.jpm.core.jpm_repository.generator.infra.utils.ColumnResolver;
import io.jpm.common.utils.LogPrinter;

import java.util.List;

public class JoinNode implements SqlNode {
    private final String joinType; // "INNER JOIN" 또는 "LEFT JOIN"
    private final String rawClass;
    private final String leftCol;
    private final String rightCol;
    private final ColumnResolver columnResolver;

    private final RepoMetaRegistry repoMetaRegistry;

    // 1. 생성자에서는 넘어온 값들을 멤버 변수로 세팅만 해줍니다.
    public JoinNode(String cmd, List<String> args, ColumnResolver columnResolver, RepoMetaRegistry repoMetaRegistry) {
        this.joinType = "leftJoin".equals(cmd) ? "LEFT JOIN" : "INNER JOIN";
        this.rawClass = args.get(0);
        this.leftCol = args.get(1);
        this.rightCol = args.get(2);
        this.columnResolver = columnResolver;
        this.repoMetaRegistry = repoMetaRegistry;
    }

    @Override
    public void apply(BuildContext ctx) {


        try {
            String cleanedClass = cleanClassName(this.rawClass);

            EntityMeta meta = repoMetaRegistry.getEntityMeta(cleanedClass);
            String actualTable = (meta != null) ? repoMetaRegistry.getTable(meta.getTableName()) : cleanedClass;


            String resolvedLeftCol = columnResolver.resolve(leftCol, ctx);
            String resolvedRightCol = columnResolver.resolve(rightCol, ctx);

            assert meta != null;
            String alias = "";

            String joinStr = this.joinType + " " + actualTable + " " + alias + "ON " + resolvedLeftCol + " = " + resolvedRightCol;


            ctx.getJoins().add(joinStr);
        }
        catch (Exception e) {

            LogPrinter.exceptionInfo(e);
            throw e;
        }
    }



    @Override
    public String toSql(BuildContext ctx) {

        return "";
    }

    // -------------------------------------------------------------------------
    // 내부 헬퍼 메서드
    // -------------------------------------------------------------------------

    private String cleanClassName(String raw) {
        if (raw.startsWith("class ")) raw = raw.substring(raw.lastIndexOf('.') + 1);
        return raw.replace(".class", "");
    }





}