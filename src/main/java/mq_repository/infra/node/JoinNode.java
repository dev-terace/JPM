package mq_repository.infra.node;

import config.AppConfig;
import mq_mapper.infra.repo.EntityMetaRegistry;
import mq_mapper.domain.policy.prev.SqlMapperBinderImpl;

import mq_repository.domain.BuildContext;
import mq_repository.domain.SqlNode;
import mq_mapper.domain.vo.EntityMeta;
import mq_repository.infra.utils.ColumnResolver;
import utils.LogPrinter;

import java.util.List;

public class JoinNode implements SqlNode {
    private final String joinType; // "INNER JOIN" 또는 "LEFT JOIN"
    private final String rawClass;
    private final String leftCol;
    private final String rightCol;


    private final EntityMetaRegistry entityMetaRegistry = AppConfig.getEntityMetaRegistry();

    // 1. 생성자에서는 넘어온 값들을 멤버 변수로 세팅만 해줍니다.
    public JoinNode(String cmd, List<String> args) {
        this.joinType = "leftJoin".equals(cmd) ? "LEFT JOIN" : "INNER JOIN";
        this.rawClass = args.get(0);
        this.leftCol = args.get(1);
        this.rightCol = args.get(2);
    }

    @Override
    public void apply(BuildContext ctx) {
        String cleanedClass = cleanClassName(this.rawClass);

        LogPrinter.info("[joinNode] rawClass" + cleanedClass);
        EntityMeta meta = entityMetaRegistry.getEntityMeta(cleanedClass);
        String actualTable = (meta != null) ? entityMetaRegistry.getTable(meta.getTableName()) : cleanedClass;

        String resolvedLeftCol = ColumnResolver.resolve(leftCol, ctx);
        String resolvedRightCol = ColumnResolver.resolve(rightCol, ctx);


        LogPrinter.info("[joinNode] resolvedLeftCol : "  + resolvedLeftCol);
        LogPrinter.info("[joinNode] resolvedRightCol : "  + resolvedRightCol);

        String[] colonParts = leftCol.split("::");
        String prefix = colonParts[0];
        assert meta != null;
        String leftColTable = prefix.equals(meta.getTableName())
                ? prefix
                : prefix.contains(".") ? prefix.split("\\.")[1] : prefix;


        String alias = "";


        LogPrinter.info("[joinNode] leftColTable : "  + leftColTable + "meta table Name : " + meta.getTableName());

        LogPrinter.info("[joinNode] leftCol : "  + entityMetaRegistry.getTable(leftColTable));
        String leftColTableName = entityMetaRegistry.getTable(leftColTable);

        if(leftColTable.equals(meta.getTableName()) || leftColTableName.equals(meta.getTableName()))
        {
            if(prefix.contains(".")) alias = resolvedLeftCol.split("\\.")[0] + " ";
        }
        else{
            throw new RuntimeException("1번째 인자 값과 2번째 인자 값이 같도록 설정 해주세요.");
        }

        String joinStr = this.joinType + " " + actualTable +" "+ alias + "ON " + resolvedLeftCol + " = " + resolvedRightCol;


        ctx.getJoins().add(joinStr);
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

    private String forceResolveColumn(String colStr, BuildContext ctx) {
        if (!colStr.contains(".")) return colStr; // 단순 문자열이면 그냥 리턴

        String[] parts = colStr.split("\\.");
        String alias = parts[0];
        String fieldName = parts[1];

        // Alias를 통해 실제 테이블명 획득
        String tableName = ctx.getTableAliases().get(alias);
        LogPrinter.info("[joinNode] tableName : " + tableName);

        if (tableName != null) {
            EntityMeta meta = entityMetaRegistry.getEntityMeta(tableName);
            if (meta != null) {
                String dbCol = meta.getColumn(fieldName);
                if (dbCol != null) {
                    return alias + "." + dbCol;
                }
            }
        }
        return colStr; // 못 찾으면 원래 문자열 리턴
    }

    // 🚀 [추가됨] 기존 원본 클래스에 있던 correctRightCol을 노드 안으로 가져왔습니다.
    private String correctRightCol(String rightCol, String rightAlias) {
        if (rightCol == null) return "";

        // 이미 "alias.column" 형태인 경우
        if (rightCol.contains(".")) {
            String[] parts = rightCol.split("\\.");
            // 만약 앞부분이 현재 정해진 별칭과 다르다면 교정
            if (!parts[0].equals(rightAlias)) {
                return rightAlias + "." + parts[1];
            }
            return rightCol;
        }

        // 별칭이 있는 경우에만 접두어 추가
        return (rightAlias != null && !rightAlias.isEmpty())
                ? rightAlias + "." + rightCol
                : rightCol;
    }


}