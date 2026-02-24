package mq_mapper.domain.policy;

import mq_mapper.domain.vo.DslStatement;
import mq_mapper.domain.vo.EntityMeta;
import mq_mapper.infra.repo.EntityMetaRegistry;
import mq_repository.domain.BuildContext;
import mq_repository.domain.SqlNode;
import mq_repository.domain.enums.GroupType;
import mq_repository.infra.node.*;
import mq_repository.infra.utils.ColumnResolver;
import utils.LogPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link DslStatement} 목록을 {@link SqlNode} 트리로 변환합니다.
 *
 * <p>기존 {@code parseToNodes} / {@code parseToGroupNode} / {@code handleMapJoin}
 * 메서드를 단일 책임 원칙에 따라 분리한 클래스입니다.
 */
public class SqlNodeParser {

    private final EntityMetaRegistry entityMetaRegistry;
    private final ArgResolver argResolver;

    public SqlNodeParser(EntityMetaRegistry entityMetaRegistry, ArgResolver argResolver) {
        this.entityMetaRegistry = entityMetaRegistry;
        this.argResolver        = argResolver;
    }

    // -------------------------------------------------------------------------
    // 공개 API
    // -------------------------------------------------------------------------

    public List<SqlNode> parse(List<DslStatement> statements, BuildContext ctx, EntityMeta entityMeta) {
        try {
            detectJoinAndMarkPrefix(statements, ctx);

            List<SqlNode> nodes = new ArrayList<>();
            WhereClauseNode whereClause = new WhereClauseNode();

            for (int i = 0; i < statements.size(); i++) {
                DslStatement stmt = statements.get(i);
                String cmd = stmt.getCommand();

                if ("mapJoin".equals(cmd)) {
                    handleMapJoin(stmt, ctx, entityMeta);
                    continue;
                }

                List<String> args = argResolver.resolveAll(stmt.getArgs(), entityMeta, ctx);
                LogPrinter.info("[SqlNodeParser] cmd=" + cmd + " args=" + args);

                switch (cmd) {
                    // ── SELECT ─────────────────────────────────────────────
                    case "select":
                    case "selectRaw":
                        nodes.add(new SelectNode(stmt.getArgs()));
                        break;

                    // ── FROM ───────────────────────────────────────────────
                    case "from":
                        args.set(0, resolveTableName(cleanClassName(args.get(0))));
                        nodes.add(new TableNode(args));
                        break;

                    case "fromGroup": {
                        List<DslStatement> sub = GroupExtractor.extract(statements, i);
                        i += sub.size() + 1;
                        nodes.add(new FromSubQueryNode(stmt, sub, entityMeta));
                        break;
                    }

                    // ── JOIN ───────────────────────────────────────────────
                    case "innerJoin":
                    case "leftJoin":
                        nodes.add(new JoinNode(cmd, stmt.getArgs()));
                        break;

                    case "innerJoinGroup":
                    case "leftJoinGroup": {
                        List<DslStatement> sub = GroupExtractor.extract(statements, i);
                        i += sub.size() + 1;
                        nodes.add(new JoinGroupNode(cmd, args, sub, entityMeta));
                        break;
                    }

                    // ── WHERE ──────────────────────────────────────────────
                    case "where":
                    case "and":
                        whereClause.addCondition(buildCondition("AND", stmt, args, ctx));
                        break;

                    case "or":
                        whereClause.addCondition(buildCondition("OR", stmt, args, ctx));
                        break;

                    case "andGroup":
                    case "orGroup":
                    case "group": {
                        List<DslStatement> sub = GroupExtractor.extract(statements, i);
                        i += sub.size() + 1;
                        GroupType type = cmd.startsWith("or") ? GroupType.OR : GroupType.AND;
                        whereClause.addGroup(parseGroup(sub, type, ctx, entityMeta));
                        break;
                    }

                    case "whereExistsGroup":
                    case "whereNotExistsGroup": {
                        List<DslStatement> sub = GroupExtractor.extract(statements, i);
                        i += sub.size() + 1;
                        ctx.markRequiresPrefix();
                        whereClause.addCondition(new ExistsNode(cmd, sub, entityMeta));
                        break;
                    }

                    // ── DML ────────────────────────────────────────────────
                    case "update":
                        nodes.add(new ActionNode("UPDATE"));
                        break;
                    case "deleteFrom":
                        nodes.add(new ActionNode("DELETE"));
                        break;
                    case "insertInto":
                        nodes.add(new InsertNode(args));
                        break;

                    case "set":
                    case "setRaw":
                        nodes.add(new SetNode(
                                stmt.getArgs().get(0),
                                resolveSqlValue(stmt.getArgs().get(0), args.get(1), ctx)
                        ));
                        break;

                    case "value":
                        nodes.add(new ValueNode(
                                stmt.getArgs().get(0),
                                resolveSqlValue(stmt.getArgs().get(0), args.get(1), ctx)
                        ));
                        break;

                    // ── 정렬 / 페이징 ──────────────────────────────────────
                    case "groupBy":  nodes.add(new GroupByNode(stmt.getArgs()));          break;
                    case "orderBy":  nodes.add(new OrderByNode(stmt.getArgs()));          break;
                    case "limit":    nodes.add(new LimitOffsetNode("LIMIT",  args.get(0))); break;
                    case "offset":   nodes.add(new LimitOffsetNode("OFFSET", args.get(0))); break;
                }
            }

            if (!whereClause.isEmpty()) nodes.add(whereClause);
            return nodes;

        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // private – 그룹 파싱
    // -------------------------------------------------------------------------

    private GroupNode parseGroup(List<DslStatement> subStatements, GroupType type,
                                 BuildContext ctx, EntityMeta entityMeta) {
        try {
            GroupNode group = new GroupNode(type);
            for (int j = 0; j < subStatements.size(); j++) {
                DslStatement s = subStatements.get(j);

                if (GroupExtractor.isGroupOpen(s.getCommand())) {
                    List<DslStatement> nested = GroupExtractor.extract(subStatements, j);
                    j += nested.size() + 1;
                    GroupType nestedType = s.getCommand().startsWith("or") ? GroupType.OR : GroupType.AND;
                    group.add(parseGroup(nested, nestedType, ctx, entityMeta));
                } else {
                    List<String> args = s.getArgs().stream()
                            .map(a -> ColumnResolver.resolve(a, ctx))
                            .collect(Collectors.toList());

                    String logic = s.getCommand().equalsIgnoreCase("or") ? "OR" : "AND";
                    if (args.size() >= 3) {
                        group.add(new ConditionNode(logic, args.get(0), args.get(1), args.get(2)));
                    }
                }
            }
            return group;
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // private – mapJoin 처리
    // -------------------------------------------------------------------------

    private void handleMapJoin(DslStatement stmt, BuildContext ctx, EntityMeta entityMeta) {
        try {
            String rawArg    = stmt.getArgs().get(0);
            String fieldName = extractFieldName(rawArg.contains("::") ? rawArg.split("::")[1] : rawArg);
            String rightAlias = stmt.getArgs().size() > 1
                    ? stmt.getArgs().get(1)
                    : "mj" + ctx.getJoins().size();

            EntityMeta targetMeta = entityMeta.getRelationTargetMeta(fieldName);
            if (targetMeta == null) {
                LogPrinter.info("[WARNING] mapJoin 타겟 메타 없음: field=" + fieldName);
                return;
            }

            ctx.registerAlias(targetMeta.getTableName(), rightAlias);

            if (rawArg.contains("::")) {
                EntityMeta parentMeta = entityMetaRegistry.getEntityMeta(rawArg.split("::")[0]);
                if (parentMeta != null && ctx.hasAlias(parentMeta.getTableName())) {
                    ctx.setTablePrefix(ctx.resolveAlias(parentMeta.getTableName()));
                }
            }
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // private – 소형 헬퍼
    // -------------------------------------------------------------------------

    private void detectJoinAndMarkPrefix(List<DslStatement> statements, BuildContext ctx) {
        for (DslStatement stmt : statements) {
            String cmd = stmt.getCommand();
            if (cmd.contains("Join") || cmd.equals("whereExistsGroup") || cmd.equals("whereNotExistsGroup")) {
                ctx.markRequiresPrefix();
                return;
            }
        }
    }

    private ConditionNode buildCondition(String logic, DslStatement stmt,
                                         List<String> args, BuildContext ctx) {
        String rawValue = stmt.getArgs().size() > 2 ? stmt.getArgs().get(2) : args.get(2);
        return new ConditionNode(
                logic,
                stmt.getArgs().get(0),
                args.get(1),
                resolveSqlValue(stmt.getArgs().get(0), rawValue, ctx)
        );
    }

    private String resolveSqlValue(String rawLeftArg, String resolvedVal, BuildContext ctx) {
        if (resolvedVal == null || resolvedVal.trim().isEmpty()) return "NULL";
        String val = resolvedVal.trim();
        if (val.contains("#{") || (val.startsWith("'") && val.endsWith("'"))) return val;
        if (val.matches("-?\\d+(\\.\\d+)?")) return val;
        return val;
    }

    private String resolveTableName(String name) {
        if (name == null) return null;
        EntityMeta meta = entityMetaRegistry.getEntityMeta(name);
        return (meta != null && meta.getTableName() != null)
                ? entityMetaRegistry.getTable(name)
                : name;
    }

    private String cleanClassName(String raw) {
        if (raw.startsWith("class ")) raw = raw.substring(raw.lastIndexOf('.') + 1);
        return raw.replace(".class", "");
    }

    private String extractFieldName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        return methodName;
    }
}