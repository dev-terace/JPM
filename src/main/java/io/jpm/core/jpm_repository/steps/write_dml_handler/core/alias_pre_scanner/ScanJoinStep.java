package io.jpm.core.jpm_repository.steps.write_dml_handler.core.alias_pre_scanner;

import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.steps.write_dml_handler.context.AliasScanContext;

import java.util.Arrays;
import java.util.List;

public class ScanJoinStep implements Step<AliasScanContext> {

    private static final List<String> JOIN_COMMANDS = Arrays.asList("innerJoin", "leftJoin");

    @Override
    public void execute(AliasScanContext ctx) {
        for (DslStatement stmt : ctx.getStatements()) {
            if (!JOIN_COMMANDS.contains(stmt.getCommand())) continue;
            try {
                if (stmt.getArgs().size() < 3) continue;

                String     rawClass    = cleanClassName(stmt.getArgs().get(0));
                EntityMeta meta        = ctx.getRepoMetaRegistry().getEntityMeta(rawClass);
                String     actualTable = meta != null
                        ? ctx.getRepoMetaRegistry().getTable(meta.getTableName())
                        : rawClass;

                String explicitAlias = extractAliasFromRightCol(stmt.getArgs().get(2));
                String finalAlias    = nonEmpty(explicitAlias, actualTable);

                ctx.getBuildContext().registerAlias(actualTable, finalAlias);
                ctx.getBuildContext().registerAlias(rawClass,    finalAlias);
                ctx.getBuildContext().registerAlias(finalAlias,  actualTable);
            } catch (Exception e) {
                LogPrinter.exceptionInfo(e);
            }
        }
    }

    private String extractAliasFromRightCol(String rightColArg) {
        if (rightColArg.contains("|"))  return rightColArg.split("\\|")[0];
        if (rightColArg.contains(".") && !rightColArg.contains("::")) return rightColArg.split("\\.")[0];
        return null;
    }

    private String cleanClassName(String raw) {
        if (raw.startsWith("class ")) raw = raw.substring(raw.lastIndexOf('.') + 1);
        return raw.replace(".class", "");
    }

    private String nonEmpty(String candidate, String fallback) {
        return (candidate != null && !candidate.trim().isEmpty()) ? candidate : fallback;
    }
}