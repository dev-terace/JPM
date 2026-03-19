package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.alias_pre_scanner;

import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context.AliasScanContext;

import java.util.Arrays;
import java.util.List;



public class ScanJoinGroupStep implements Step<AliasScanContext> {

    private static final List<String> JOIN_GROUP_COMMANDS = Arrays.asList("innerJoinGroup", "leftJoinGroup");

    @Override
    public void execute(AliasScanContext ctx) {
        for (DslStatement stmt : ctx.getStatements()) {
            if (!JOIN_GROUP_COMMANDS.contains(stmt.getCommand())) continue;
            try {
                List<String> args = stmt.getArgs();
                if (args.size() < 3) continue;

                // JoinGroupNode 생성자와 동일한 순서: [targetClass, leftCol, rightCol]
                String rawClass  = cleanClassName(args.get(0));
                String leftCol   = args.get(1);
                String rightCol  = args.get(2);

                // ── JoinGroupNode.toSql() alias 추출 로직과 동일 ──────────────
                String alias = "sub";
                if (leftCol.contains(".")) {
                    alias = leftCol.split("\\.")[0];
                } else if (rightCol.contains("|")) {
                    alias = leftCol.split("\\|")[0];   // JoinGroupNode와 동일하게 leftCol 기준
                }
                // ─────────────────────────────────────────────────────────────

                EntityMeta meta        = ctx.getRepoMetaRegistry().getEntityMeta(rawClass);
                String     actualTable = (meta != null) ? meta.getTableName() : rawClass;

                ctx.getBuildContext().registerAlias(actualTable, alias);
                ctx.getBuildContext().registerAlias(rawClass,    alias);
                ctx.getBuildContext().registerAlias(alias,       actualTable);

            } catch (Exception e) {
                LogPrinter.exceptionInfo(e);
                throw new RuntimeException(e);
            }
        }
    }

    private String cleanClassName(String raw) {
        if (raw.startsWith("class ")) raw = raw.substring(raw.lastIndexOf('.') + 1);
        return raw.replace(".class", "");
    }
}