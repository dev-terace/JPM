package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.alias_pre_scanner;

import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context.AliasScanContext;

public class ScanFromStep implements Step<AliasScanContext> {

    @Override
    public void execute(AliasScanContext ctx) {
        for (DslStatement stmt : ctx.getStatements()) {
            if (!"from".equals(stmt.getCommand())) continue;
            try {
                if (stmt.getArgs().isEmpty()) continue;

                String     rawTable    = cleanClassName(stmt.getArgs().get(0));
                EntityMeta meta        = ctx.getRepoMetaRegistry().getEntityMeta(rawTable);
                String     actualTable = meta != null ? meta.getTableName() : rawTable;
                String     alias       = stmt.getArgs().size() >= 2
                        ? stmt.getArgs().get(1) : actualTable;

                ctx.getBuildContext().registerAlias(actualTable, alias);
                ctx.getBuildContext().registerAlias(rawTable,    alias);
                ctx.getBuildContext().registerAlias(alias,       actualTable);
            } catch (Exception e) {
                LogPrinter.exceptionInfo(e);
            }
        }
    }

    private String cleanClassName(String raw) {
        if (raw.startsWith("class ")) raw = raw.substring(raw.lastIndexOf('.') + 1);
        return raw.replace(".class", "");
    }
}