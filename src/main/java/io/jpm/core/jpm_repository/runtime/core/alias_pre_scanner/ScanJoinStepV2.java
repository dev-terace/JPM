package io.jpm.core.jpm_repository.runtime.core.alias_pre_scanner;

import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;

import io.jpm.core.jpm_repository.runtime.context.AliasScanContextV2;

import java.util.Arrays;
import java.util.List;

public class ScanJoinStepV2 implements Step<AliasScanContextV2> {

    private static final List<String> JOIN_COMMANDS = Arrays.asList("innerJoin", "leftJoin");

    @Override
    public void execute(AliasScanContextV2 ctx) {
        for (DslStatementV2 stmt : ctx.getStatements()) {
            if (!JOIN_COMMANDS.contains(stmt.getCommand())) continue;
            try {
                if (stmt.getArgs().size() < 3) continue;

                String     rawClass    = cleanClassName(stmt.getArgs().get(0).toString());

                EntityMeta meta        = ctx.getRepoMetaRegistry().getEntityMeta(rawClass);
                String     actualTable = meta != null
                        ? ctx.getRepoMetaRegistry().getTable(meta.getTableName())
                        : rawClass;

                String explicitAlias = extractAliasLeftCol(stmt.getArgs().get(1).toString());
                String finalAlias    = nonEmpty(explicitAlias, actualTable);

                ctx.getBuildContext().registerAlias(actualTable, finalAlias);
                ctx.getBuildContext().registerAlias(rawClass,    finalAlias);
                ctx.getBuildContext().registerAlias(finalAlias,  actualTable);

                System.out.println("[Alias join Register] table -> alias : " + actualTable + " -> " + finalAlias);
                System.out.println("[Alias join Register] class -> alias : " + rawClass + " -> " + finalAlias);
                System.out.println("[Alias join Register] alias -> table : " + finalAlias + " -> " + actualTable);

            } catch (Exception e) {
                LogPrinter.exceptionInfo(e);
            }
        }
    }

    private String extractAliasLeftCol(String rightColArg) {
        if (rightColArg.contains("|"))  return rightColArg.split("\\|")[0];
        if (rightColArg.contains(".") && rightColArg.contains("::")) return rightColArg.split("\\.")[0];
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