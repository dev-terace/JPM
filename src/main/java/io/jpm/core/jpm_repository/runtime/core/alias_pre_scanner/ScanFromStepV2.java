package io.jpm.core.jpm_repository.runtime.core.alias_pre_scanner;

import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;

import io.jpm.core.jpm_repository.domain.model.DslStatementV2;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.runtime.config.AppConfig;

import io.jpm.core.jpm_repository.runtime.context.AliasScanContextV2;


public class ScanFromStepV2 implements Step<AliasScanContextV2> {

    private static final RepoMetaRegistry repoMetaRegistry = AppConfig.getRepoMetaRegistry();

    @Override
    public void execute(AliasScanContextV2 ctx) {
        for (DslStatementV2 stmt : ctx.getStatements()) {
            if (!"from".equals(stmt.getCommand())) continue;

            try {
                if (stmt.getArgs().isEmpty()) continue;

                String     rawTable    = cleanClassName(stmt.getArgs().get(0).toString());
                EntityMeta meta        = repoMetaRegistry.getEntityMeta(rawTable);
                String     actualTable = meta != null ? meta.getTableName() : rawTable;
                String     alias       = stmt.getArgs().size() >= 2
                        ? stmt.getArgs().get(1).toString() : actualTable;

                ctx.getBuildContext().registerAlias(actualTable, alias);
                ctx.getBuildContext().registerAlias(rawTable,    alias);
                ctx.getBuildContext().registerAlias(alias,       actualTable);





            } catch (Exception e) {
               System.err.println(e.getMessage());
            }
        }
    }

    private String cleanClassName(String raw) {
        if (raw.startsWith("class ")) raw = raw.substring(raw.lastIndexOf('.') + 1);
        return raw.replace(".class", "");
    }
}