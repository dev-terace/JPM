package jpm_repository.parse.domain.policy.sql_node_parser.node;

import config.AppConfig;
import exception.ErrorCollector;
import jpm_repository.parse.domain.cache.RepoMetaRegistry;

import jpm_repository.parse.domain.cache.EntityRelationRegistry;
import jpm_repository.parse.domain.vo.BuildContext;
import jpm_repository.parse.domain.vo.EntityMeta;
import jpm_repository.generator.infra.utils.ColumnResolver;
import jpm_repository.valid.policy.JoinNodeValidatorPolicy;
import utils.LogPrinter;

import java.util.List;

public class JoinNode implements SqlNode {
    private final String joinType; // "INNER JOIN" 또는 "LEFT JOIN"
    private final String rawClass;
    private final String leftCol;
    private final String rightCol;


    private final RepoMetaRegistry repoMetaRegistry = AppConfig.getEntityMetaRegistry();
    private final EntityRelationRegistry entityRelationRegistry = AppConfig.getEntityRelationRegistry();
    // 1. 생성자에서는 넘어온 값들을 멤버 변수로 세팅만 해줍니다.
    public JoinNode(String cmd, List<String> args) {
        this.joinType = "leftJoin".equals(cmd) ? "LEFT JOIN" : "INNER JOIN";
        this.rawClass = args.get(0);
        this.leftCol = args.get(1);
        this.rightCol = args.get(2);
    }

    @Override
    public void apply(BuildContext ctx) {


        try {
            String cleanedClass = cleanClassName(this.rawClass);
            

            LogPrinter.info("[joinNode] rawClass" + cleanedClass);
            EntityMeta meta = repoMetaRegistry.getEntityMeta(cleanedClass);
            String actualTable = (meta != null) ? repoMetaRegistry.getTable(meta.getTableName()) : cleanedClass;

            String resolvedLeftCol = ColumnResolver.resolve(leftCol, ctx);
            String resolvedRightCol = ColumnResolver.resolve(rightCol, ctx);


            LogPrinter.info("[joinNode] resolvedLeftCol : " + resolvedLeftCol);
            LogPrinter.info("[joinNode] resolvedRightCol : " + resolvedRightCol);

            String[] colonParts = leftCol.split("::");
            String prefix = colonParts[0];
            assert meta != null;
            String leftColTable = prefix.equals(meta.getTableName())
                    ? prefix
                    : prefix.contains(".") ? prefix.split("\\.")[1] : prefix;


            String alias = "";


            LogPrinter.info("[joinNode] leftColTable : " + leftColTable + "meta table Name : " + meta.getTableName());

            LogPrinter.info("[joinNode] leftCol : " + repoMetaRegistry.getTable(leftColTable));



            String joinStr = this.joinType + " " + actualTable + " " + alias + "ON " + resolvedLeftCol + " = " + resolvedRightCol;


            ctx.getJoins().add(joinStr);
        }
        catch (Exception e) {

            LogPrinter.exceptionInfo(e);
            throw e;
        }
    }

  /*  private void validateJoinType(String leftCol, String rightCol)
    {

        String[] leftColInfo = ColumnResolver.resolve(leftCol);
        String[] rightColInfo = ColumnResolver.resolve(rightCol);
        String leftEntityName = leftColInfo[0];
        String rightEntityName = rightColInfo[0];
        String leftFieldName = leftColInfo[1];
        String rightFieldName = rightColInfo[1];

        EntityMeta leftColMeta = entityMetaRegistry.getEntityMeta(leftEntityName);
        EntityMeta rightColMeta = entityMetaRegistry.getEntityMeta(rightEntityName);


        String leftFieldType = leftColMeta.getFieldType(leftFieldName);
        String rightFieldType = rightColMeta.getFieldType(rightFieldName);

        validateFkTypeMatch(leftEntityName, leftFieldType, rightEntityName, rightFieldType, leftFieldName, rightFieldName);

    }

    private void validateFkTypeMatch(
            String leftEntityName, String leftColFieldType,
            String rightEntityName, String rightColFieldType) {

        // leftCol이 FK인 경우
        entityRelationRegistry.getPkFieldType(leftEntityName);


        entityRelationRegistry.getPkFieldName(leftEntityName);
        entityRelationRegistry.getPkFieldName(rightEntityName);




        if ("FK".equals(leftColFieldType)) {
            String leftParentFieldType = entityRelationRegistry.resolveFkType(leftEntityName, leftColFieldType);
            if (!rightColFieldType.equals(leftParentFieldType)) {
                throw new RuntimeException(
                        "타입 불일치: leftCol FK의 부모 타입=" + leftParentFieldType +
                                ", rightColFieldType=" + rightColFieldType
                );
            }
        }

        // rightCol이 FK인 경우
        if ("FK".equals(rightColFieldType)) {
            String rightParentFieldType = entityRelationRegistry.resolveFkType(rightEntityName, rightColFieldType);
            if (!leftColFieldType.equals(rightParentFieldType)) {
                throw new RuntimeException(
                        "타입 불일치: rightCol FK의 부모 타입=" + rightParentFieldType +
                                ", leftColFieldType=" + leftColFieldType
                );
            }
        }
    }*/


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

    private String forceResolveColumn(String colStr, BuildContext ctx) {
        if (!colStr.contains(".")) return colStr; // 단순 문자열이면 그냥 리턴

        String[] parts = colStr.split("\\.");
        String alias = parts[0];
        String fieldName = parts[1];

        // Alias를 통해 실제 테이블명 획득
        String tableName = ctx.getTableAliases().get(alias);
        LogPrinter.info("[joinNode] tableName : " + tableName);

        if (tableName != null) {
            EntityMeta meta = repoMetaRegistry.getEntityMeta(tableName);
            if (meta != null) {
                String dbCol = meta.getColumn(fieldName);
                if (dbCol != null) {
                    return alias + "." + dbCol;
                }
            }
        }
        return colStr; // 못 찾으면 원래 문자열 리턴
    }



}