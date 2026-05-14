package io.jpm.core.jpm_repository.runtime.core;

import io.jpm.common.utils.CustomLogger;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.enums.GroupType;
import io.jpm.core.jpm_repository.domain.model.BuildContext;

import io.jpm.core.jpm_repository.domain.model.DslStatementV2;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;

import io.jpm.core.jpm_repository.runtime.cache.ResultMappingMeta;
import io.jpm.core.jpm_repository.runtime.context.SqlNodeParserContextV2;
import io.jpm.core.jpm_repository.runtime.core.node.ExistsNodeV2;
import io.jpm.core.jpm_repository.runtime.core.node.FromSubQueryNodeV2;
import io.jpm.core.jpm_repository.runtime.core.node.JoinGroupNodeV2;
import io.jpm.core.jpm_repository.runtime.core.node.SelectNodeV2;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node.*;

import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.ArgResolver;

import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.utils.GroupExtractorUtil;
import io.jpm.core.jpm_repository.utils.ColumnResolver;


import java.util.List;

import java.util.stream.Collectors;


//리플렉션으로 함수형 추출하기 + 캐싱

public class BuildSqlNodesStepV2 implements Step<SqlNodeParserContextV2> {

    private final RepoMetaRegistry repoMetaRegistry;
    private final ArgResolver argResolver;
    private final ColumnResolver   columnResolver;
    private final SqlNodeParserStepsV2 sqlNodeParserStepsV2;
    private final CustomLogger log = CustomLogger.getLogger(BuildSqlNodesStepV2.class);



    public BuildSqlNodesStepV2(RepoMetaRegistry repoMetaRegistry, SqlNodeParserStepsV2 sqlNodeParserStepsV2) {

        this.repoMetaRegistry = repoMetaRegistry;
        this.argResolver      = new ArgResolver(repoMetaRegistry);
        this.columnResolver   = new ColumnResolver(repoMetaRegistry);
        this.sqlNodeParserStepsV2 = sqlNodeParserStepsV2;



    }


    @Override
    public void execute(SqlNodeParserContextV2 ctx) {

        List<DslStatementV2> statements = ctx.getStatements();
        BuildContext       buildCtx   = ctx.getBuildContext();
        WhereClauseNode    where      = new WhereClauseNode();

        HavingClauseNode having = new HavingClauseNode();


        String entityName = "";

        for (DslStatementV2 stmt : statements) {
            String cmd = stmt.getCommand();
            if(cmd.equals("from"))
            {
                entityName = stmt.getArgs().get(0).toString()
                        .replace("class ", "")
                        .trim()
                        .split("\\.")[1];

                log.debug("entityName = {}", entityName);
                break;
            }

        }

        EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(entityName);

        List<ResultMappingMeta> resultMappingMetas = ctx.getResultMappingMeta();


        for (int i = 0; i < statements.size(); i++) {
            DslStatementV2 stmt = statements.get(i);
            String cmd = stmt.getCommand();
            log.debug("cmd : {}", cmd);

            List<String> resolveStmt = DslStatementArgResolver.resolve(stmt);
            List<String> args = argResolver.resolveAll(resolveStmt, entityMeta, buildCtx);
            //ResultMappingMeta ADD func 구현

            log.debug("args : {}", args);


            switch (cmd) {
                case "select":
                    ctx.addNode(new SelectNodeV2(args, entityMeta, resultMappingMetas, buildCtx.getTableAliases(), entityName));
                    log.debug("selected entity : {}", entityName);
                    break;

                case "selectRaw":
                    ctx.addNode(new SelectRawNode(resolveStmt, columnResolver));
                    break;

                case "selectRawResult":
                    ctx.addNode(new SelectRawResultNode(resolveStmt, columnResolver));
                    break;

                case "from":
                    args.set(0, resolveTableName(cleanClassName(args.get(0))));
                    ctx.addNode(new TableNode(args, repoMetaRegistry));
                    break;

                case "fromGroup": {
                    List<DslStatementV2> sub = GroupExtractorUtil.extractV2(statements, i);
                    i += sub.size() + 1;
                    ctx.addNode(new FromSubQueryNodeV2(stmt, sub, entityMeta, sqlNodeParserStepsV2));
                    break;
                }

                case "innerJoin":
                case "leftJoin":
                    ctx.addNode(new JoinNode(cmd, resolveStmt, columnResolver, repoMetaRegistry));
                    break;

                case "innerJoinGroup":
                case "leftJoinGroup": {
                    List<DslStatementV2> sub = GroupExtractorUtil.extractV2(statements, i);
                    i += sub.size() + 1;
                    ctx.addNode(new JoinGroupNodeV2(cmd, args, sub, entityMeta, sqlNodeParserStepsV2, repoMetaRegistry));
                    break;
                }

                case "where":
                case "whereAnd":
                    where.addCondition(buildCondition(" AND ", args));
                    break;

                case "whereOr":
                    where.addCondition(buildCondition(" OR ", args));
                    break;

                case "andGroup":
                case "orGroup":
                case "group": {
                    List<DslStatementV2> sub = GroupExtractorUtil.extractV2(statements, i);
                    i += sub.size() + 1;
                    GroupType type = cmd.startsWith("or") ? GroupType.OR : GroupType.AND;
                    where.addGroup(parseGroup(sub, type, buildCtx));
                    break;
                }

                case "whereExistsGroup":
                case "whereNotExistsGroup": {
                    List<DslStatementV2> sub = GroupExtractorUtil.extractV2(statements, i);
                    i += sub.size() + 1;
                    buildCtx.markRequiresPrefix();
                    where.addCondition(new ExistsNodeV2(cmd, sub, entityMeta, repoMetaRegistry, sqlNodeParserStepsV2));
                    break;
                }


                case "update":    ctx.addNode(new ActionNode("UPDATE")); break;
                case "deleteFrom": ctx.addNode(new ActionNode("DELETE")); break;
                case "insertInto": ctx.addNode(new InsertNode(args));     break;

                case "set":
                case "setRaw":
                    ctx.addNode(new SetNode(resolveStmt.get(0),
                            resolveSqlValue(args.get(1)), columnResolver));
                    break;

                case "value":
                    ctx.addNode(new ValueNode(resolveStmt.get(0),
                            resolveSqlValue(args.get(1)), columnResolver));
                    break;

                case "groupBy": ctx.addNode(new GroupByNode(resolveStmt, repoMetaRegistry, columnResolver)); break;
                case "orderBy": ctx.addNode(new OrderByNode(resolveStmt, columnResolver));                   break;
                case "limit":   ctx.addNode(new LimitOffsetNode("LIMIT",  args.get(0)));                       break;
                case "offset":  ctx.addNode(new LimitOffsetNode("OFFSET", args.get(0)));                       break;

                case "having":
                case "havingAnd":
                    having.addCondition(buildCondition(" AND ", args));
                    break;

                case "havingOr":
                    having.addCondition(buildCondition(" OR ",  args));
                    break;

                case "havingGroup":
                case "havingOrGroup": {
                    List<DslStatementV2> sub = GroupExtractorUtil.extractV2(statements, i);
                    i += sub.size() + 1;
                    GroupType type = cmd.startsWith("havingOr") ? GroupType.OR : GroupType.AND;
                    having.addGroup(parseGroup(sub, type, buildCtx));
                    break;
                }

            }

        }


        if (!where.isEmpty()) ctx.addNode(where);
        if (!having.isEmpty()) ctx.addNode(having);


    }

    // -------------------------------------------------------------------------
    // Sub-steps (루프 안에서 호출되므로 private 위임)
    // -------------------------------------------------------------------------



    private GroupNode parseGroup(List<DslStatementV2> subStatements, GroupType type,
                                 BuildContext ctx) {
        try {
            GroupNode group = new GroupNode(type);
            for (int j = 0; j < subStatements.size(); j++) {
                DslStatementV2 s = subStatements.get(j);

                if (GroupExtractorUtil.isGroupOpen(s.getCommand())) {
                    List<DslStatementV2> nested = GroupExtractorUtil.extractV2(subStatements, j);
                    j += nested.size() + 1;
                    GroupType nestedType = s.getCommand().startsWith("or") ? GroupType.OR : GroupType.AND;
                    group.add(parseGroup(nested, nestedType, ctx));
                } else {
                    List<String> args = s.getArgs().stream()
                            .map(a -> columnResolver.resolve(a.toString(), ctx))
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

    private ConditionNode buildCondition(String logic,
                                         List<String> args) {
        String rawValue = args.get(2);
        return new ConditionNode(
                args.get(0),
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


    }
