package mq_repository.infra.node;

import config.AppConfig;
import mq_mapper.infra.repo.EntityMetaRegistry;
import mq_mapper.domain.policy.prev.SqlMapperBinderImpl;
import mq_repository.domain.BuildContext;
import mq_repository.domain.SqlNode;
import mq_mapper.domain.vo.EntityMeta;
import utils.LogPrinter;

import java.util.List;


public class TableNode implements SqlNode {
    private final List<String> args;

    public TableNode(List<String> args) {
        this.args = args;
    }

    private static final EntityMetaRegistry entityMetaRegistry = AppConfig.getEntityMetaRegistry();

    @Override
    public void apply(BuildContext ctx) {
        String inputName = args.get(0); // "UsersInfoEntity" 또는 "users_info"
        String alias = args.size() > 1 ? args.get(1) : null;

        // 🚀 1. 클래스명으로 Meta를 찾아서 실제 테이블명 추출

        LogPrinter.info("[TableNode] alias=" + inputName);
        EntityMeta meta = entityMetaRegistry.getEntityMeta(inputName);
        String finalTableName = (meta != null) ? meta.getTableName() : inputName;

        // 2. 테이블 표현식 조립
        String tableExpr = finalTableName;
        if (alias != null) {
            tableExpr += " " + alias;
            ctx.getTableAliases().put(alias, finalTableName);
        } else {
            ctx.getTableAliases().put(finalTableName, finalTableName);
        }

        // 3. Set에 추가 (중복 방지)
        ctx.getTables().add(tableExpr);

        // 4. 접두어 설정
        if (ctx.getTablePrefix() == null || ctx.getTablePrefix().isEmpty()) {
            ctx.setTablePrefix(alias != null ? alias : finalTableName);
        }



    }

    @Override
    public String toSql(BuildContext ctx) {
        return "";
    }
}
