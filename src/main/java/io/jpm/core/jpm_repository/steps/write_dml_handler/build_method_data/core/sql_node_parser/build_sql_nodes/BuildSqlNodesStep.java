package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes;

import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.enums.GroupType;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context.SqlNodeParserContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.SqlNodeParserSteps;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node.*;

import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.ArgResolver;

import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.utils.GroupExtractorUtil;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.List;
import java.util.stream.Collectors;

public class BuildSqlNodesStep implements Step<SqlNodeParserContext> {

    private final RepoMetaRegistry repoMetaRegistry;
    private final ArgResolver argResolver;
    private final ColumnResolver   columnResolver;
    private final SqlNodeParserSteps  nodeParser;

    public BuildSqlNodesStep(RepoMetaRegistry repoMetaRegistry,
                             ArgResolver argResolver,
                             ColumnResolver columnResolver,
                             SqlNodeParserSteps nodeParser) {
        this.repoMetaRegistry = repoMetaRegistry;
        this.argResolver      = argResolver;
        this.columnResolver   = columnResolver;
        this.nodeParser           = nodeParser;
    }

    @Override
    public void execute(SqlNodeParserContext ctx) {
        List<DslStatement> statements = ctx.getStatements();
        BuildContext       buildCtx   = ctx.getBuildContext();
        EntityMeta         entityMeta = ctx.getEntityMeta();
        WhereClauseNode    where      = new WhereClauseNode();

        for (int i = 0; i < statements.size(); i++) {
            DslStatement stmt = statements.get(i);
            String       cmd  = stmt.getCommand();

            if ("mapJoin".equals(cmd)) {
                handleMapJoin(stmt, buildCtx, entityMeta);
                continue;
            }

            List<String> args = argResolver.resolveAll(stmt.getArgs(), entityMeta, buildCtx);

            switch (cmd) {
                case "select":
                    ctx.addNode(new SelectNode(stmt.getArgs(), columnResolver));
                    break;
                case "selectRaw":
                    ctx.addNode(new SelectRawNode(stmt.getArgs(), columnResolver));
                    break;

                case "from":
                    args.set(0, resolveTableName(cleanClassName(args.get(0))));
                    ctx.addNode(new TableNode(args, repoMetaRegistry));
                    break;

                case "fromGroup": {
                    List<DslStatement> sub = GroupExtractorUtil.extract(statements, i);
                    i += sub.size() + 1;
                    ctx.addNode(new FromSubQueryNode(stmt, sub, entityMeta, nodeParser));
                    break;
                }

                case "innerJoin":
                case "leftJoin":
                    ctx.addNode(new JoinNode(cmd, stmt.getArgs(), columnResolver, repoMetaRegistry));
                    break;

                case "innerJoinGroup":
                case "leftJoinGroup": {
                    List<DslStatement> sub = GroupExtractorUtil.extract(statements, i);
                    i += sub.size() + 1;
                    ctx.addNode(new JoinGroupNode(cmd, args, sub, entityMeta, nodeParser, repoMetaRegistry));
                    break;
                }

                case "where":
                case "and":
                    where.addCondition(buildCondition(" AND ", stmt, args, buildCtx));
                    break;

                case "or":
                    where.addCondition(buildCondition(" OR ", stmt, args, buildCtx));
                    break;

                case "andGroup":
                case "orGroup":
                case "group": {
                    List<DslStatement> sub = GroupExtractorUtil.extract(statements, i);
                    i += sub.size() + 1;
                    GroupType type = cmd.startsWith("or") ? GroupType.OR : GroupType.AND;
                    where.addGroup(parseGroup(sub, type, buildCtx, entityMeta));
                    break;
                }

                case "whereExistsGroup":
                case "whereNotExistsGroup": {
                    List<DslStatement> sub = GroupExtractorUtil.extract(statements, i);
                    i += sub.size() + 1;
                    buildCtx.markRequiresPrefix();
                    where.addCondition(new ExistsNode(cmd, sub, entityMeta, repoMetaRegistry, nodeParser));
                    break;
                }

                case "update":    ctx.addNode(new ActionNode("UPDATE")); break;
                case "deleteFrom": ctx.addNode(new ActionNode("DELETE")); break;
                case "insertInto": ctx.addNode(new InsertNode(args));     break;

                case "set":
                case "setRaw":
                    ctx.addNode(new SetNode(stmt.getArgs().get(0),
                            resolveSqlValue(args.get(1)), columnResolver));
                    break;

                case "value":
                    ctx.addNode(new ValueNode(stmt.getArgs().get(0),
                            resolveSqlValue(args.get(1)), columnResolver));
                    break;

                case "groupBy": ctx.addNode(new GroupByNode(stmt.getArgs(), repoMetaRegistry, columnResolver)); break;
                case "orderBy": ctx.addNode(new OrderByNode(stmt.getArgs(), columnResolver));                   break;
                case "limit":   ctx.addNode(new LimitOffsetNode("LIMIT",  args.get(0)));                       break;
                case "offset":  ctx.addNode(new LimitOffsetNode("OFFSET", args.get(0)));                       break;
            }
        }

        if (!where.isEmpty()) ctx.addNode(where);
    }

    // -------------------------------------------------------------------------
    // Sub-steps (루프 안에서 호출되므로 private 위임)
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
                EntityMeta parentMeta = repoMetaRegistry.getEntityMeta(rawArg.split("::")[0]);
                if (parentMeta != null && ctx.hasAlias(parentMeta.getTableName())) {
                    ctx.setTablePrefix(ctx.resolveAlias(parentMeta.getTableName()));
                }
            }
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    private GroupNode parseGroup(List<DslStatement> subStatements, GroupType type,
                                 BuildContext ctx, EntityMeta entityMeta) {
        try {
            GroupNode group = new GroupNode(type);
            for (int j = 0; j < subStatements.size(); j++) {
                DslStatement s = subStatements.get(j);

                if (GroupExtractorUtil.isGroupOpen(s.getCommand())) {
                    List<DslStatement> nested = GroupExtractorUtil.extract(subStatements, j);
                    j += nested.size() + 1;
                    GroupType nestedType = s.getCommand().startsWith("or") ? GroupType.OR : GroupType.AND;
                    group.add(parseGroup(nested, nestedType, ctx, entityMeta));
                } else {
                    List<String> args = s.getArgs().stream()
                            .map(a -> columnResolver.resolve(a, ctx))
                            .collect(Collectors.toList());
                    String logic = s.getCommand().equalsIgnoreCase("or") ? " OR " : " AND ";
                    if (args.size() >= 3) {
                        LogPrinter.info("parse[group]" + args);
                        group.add(new ConditionNode(args.get(0), args.get(1), args.get(2), logic, columnResolver));
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
    // Helpers
    // -------------------------------------------------------------------------

    private ConditionNode buildCondition(String logic, DslStatement stmt,
                                         List<String> args, BuildContext ctx) {
        String rawValue = stmt.getArgs().size() > 2 ? stmt.getArgs().get(2) : args.get(2);
        return new ConditionNode(
                stmt.getArgs().get(0),
                args.get(1),
                resolveSqlValue(rawValue),
                logic,
                columnResolver
        );
    }

    private String resolveSqlValue(String resolvedVal) {
        if (resolvedVal == null || resolvedVal.trim().isEmpty()) return "NULL";
        String val = resolvedVal.trim();
        if (val.contains("#{") || (val.startsWith("'") && val.endsWith("'"))) return val;
        if (val.matches("-?\\d+(\\.\\d+)?")) return val;
        return val;
    }

    private String resolveTableName(String name) {
        if (name == null) return null;
        EntityMeta meta = repoMetaRegistry.getEntityMeta(name);
        return (meta != null && meta.getTableName() != null)
                ? repoMetaRegistry.getTable(name)
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