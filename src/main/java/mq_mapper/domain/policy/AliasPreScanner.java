package mq_mapper.domain.policy;

import mq_mapper.domain.vo.DslStatement;
import mq_mapper.domain.vo.EntityMeta;
import mq_mapper.infra.repo.EntityMetaRegistry;
import mq_repository.domain.BuildContext;
import utils.LogPrinter;

import java.util.List;

/**
 * FROM / JOIN 선언을 미리 읽어 {@link BuildContext}의 테이블 별칭 맵을 구성합니다.
 *
 * <p>기존 preScanAliases / preScanFrom / preScanJoin / preScanJoinGroup 메서드를
 * 단일 책임 원칙에 따라 분리한 클래스입니다.
 */
public class AliasPreScanner {

    private final EntityMetaRegistry entityMetaRegistry;

    public AliasPreScanner(EntityMetaRegistry entityMetaRegistry) {
        this.entityMetaRegistry = entityMetaRegistry;
    }

    public void scan(List<DslStatement> statements, BuildContext ctx) {
        try {
            int groupDepth = 0;
            for (int i = 0; i < statements.size(); i++) {
                DslStatement stmt = statements.get(i);
                String cmd = stmt.getCommand();

                if (groupDepth == 0) {
                    switch (cmd) {
                        case "from":          scanFrom(stmt, ctx);      break;
                        case "innerJoin":
                        case "leftJoin":      scanJoin(stmt, ctx);      break;
                        case "innerJoinGroup":
                        case "leftJoinGroup": scanJoinGroup(stmt, ctx); break;
                    }

                    // 그룹 내부도 재귀 스캔
                    if (isGroupOpen(cmd)) {
                        List<DslStatement> subStmts = GroupExtractor.extract(statements, i);
                        scan(subStmts, ctx);
                    }
                }

                if (isGroupOpen(cmd))         groupDepth++;
                else if ("endGroup".equals(cmd)) groupDepth--;
            }
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
        }
    }

    // -------------------------------------------------------------------------
    // private scanners
    // -------------------------------------------------------------------------

    private void scanFrom(DslStatement stmt, BuildContext ctx) {
        try {
            if (stmt.getArgs().isEmpty()) return;

            String rawTable   = cleanClassName(stmt.getArgs().get(0));
            EntityMeta meta   = entityMetaRegistry.getEntityMeta(rawTable);
            String actualTable = meta != null ? meta.getTableName() : rawTable;
            String alias      = stmt.getArgs().size() >= 2 ? stmt.getArgs().get(1) : actualTable;

            LogPrinter.info("[AliasPreScanner.from] rawTable=" + rawTable
                    + " actual=" + actualTable + " alias=" + alias);

            ctx.registerAlias(actualTable, alias);
            ctx.registerAlias(rawTable,    alias);
            ctx.registerAlias(alias,       actualTable);
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
        }
    }

    private void scanJoin(DslStatement stmt, BuildContext ctx) {
        try {
            if (stmt.getArgs().size() < 3) return;

            String rawClass    = cleanClassName(stmt.getArgs().get(0));
            EntityMeta meta    = entityMetaRegistry.getEntityMeta(rawClass);
            String actualTable = meta != null
                    ? entityMetaRegistry.getTable(meta.getTableName())
                    : rawClass;

            String explicitAlias = extractAliasFromRightCol(stmt.getArgs().get(2));
            String finalAlias    = nonEmpty(explicitAlias, actualTable);

            ctx.registerAlias(actualTable, finalAlias);
            ctx.registerAlias(rawClass,    finalAlias);
            ctx.registerAlias(finalAlias,  actualTable);

            LogPrinter.info("[AliasPreScanner.join] rawClass=" + rawClass + " alias=" + finalAlias);
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
        }
    }

    private void scanJoinGroup(DslStatement stmt, BuildContext ctx) {
        try {
            if (stmt.getArgs().size() < 3) return;

            String rawClass    = cleanClassName(stmt.getArgs().get(0));
            EntityMeta meta    = entityMetaRegistry.getEntityMeta(rawClass);
            String actualTable = meta != null ? meta.getTableName() : rawClass;

            String explicitAlias = extractAliasFromRightCol(stmt.getArgs().get(2));
            if (explicitAlias == null) {
                explicitAlias = findPureLiteralAlias(stmt.getArgs());
            }
            String finalAlias = nonEmpty(explicitAlias, actualTable + "_sub");

            ctx.registerAlias(actualTable, finalAlias);
            ctx.registerAlias(rawClass,    finalAlias);
            ctx.registerAlias(finalAlias,  actualTable);

            LogPrinter.info("[AliasPreScanner.joinGroup] rawClass=" + rawClass + " alias=" + finalAlias);
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    /**
     * 세 번째 인자(rightCol)에서 별칭을 추출합니다.
     * "alias|..." → alias, "alias.field" → alias
     */
    private String extractAliasFromRightCol(String rightColArg) {
        if (rightColArg.contains("|")) {
            return rightColArg.split("\\|")[0];
        }
        if (rightColArg.contains(".") && !rightColArg.contains("::")) {
            return rightColArg.split("\\.")[0];
        }
        return null;
    }

    /** 인자 목록에서 메서드 레퍼런스·클래스명·람다가 아닌 순수 문자열을 찾습니다. */
    private String findPureLiteralAlias(List<String> args) {
        for (int i = 1; i < args.size(); i++) {
            String a = args.get(i);
            if (!a.contains("::") && !a.endsWith(".class") && !a.contains("->")) {
                return a;
            }
        }
        return null;
    }

    private boolean isGroupOpen(String cmd) {
        return cmd.endsWith("Group") && !"endGroup".equals(cmd);
    }

    private String cleanClassName(String raw) {
        if (raw.startsWith("class ")) raw = raw.substring(raw.lastIndexOf('.') + 1);
        return raw.replace(".class", "");
    }

    private String nonEmpty(String candidate, String fallback) {
        return (candidate != null && !candidate.trim().isEmpty()) ? candidate : fallback;
    }
}