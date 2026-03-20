package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node;

import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;

import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.utils.ColumnResolver;
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
            if(resolvedLeftCol.contains(".")) {
                alias = resolvedLeftCol.split("\\.")[0] + " ";
            }

            if (actualTable.trim().equals(alias.trim())) {
                alias = "";
            }


            LogPrinter.info("Alias: " + alias + ", actual table: " + actualTable);


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