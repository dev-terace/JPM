package io.jpm.core.jpm_repository.steps.write_dml_handler.core.alias_pre_scanner;

import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.steps.write_dml_handler.context.AliasScanContext;

import java.util.List;

public class AliasPreScannerStep implements Step<AliasScanContext> {

    private final ScanFromStep      scanFromStep      = new ScanFromStep();
    private final ScanJoinStep      scanJoinStep      = new ScanJoinStep();
    private final ScanJoinGroupStep scanJoinGroupStep = new ScanJoinGroupStep();

    private final RepoMetaRegistry repoMetaRegistry;

    public AliasPreScannerStep(RepoMetaRegistry repoMetaRegistry) {
        this.repoMetaRegistry = repoMetaRegistry;
    }

    /** SqlMapperBinderImplV2 에서 직접 호출하는 기존 시그니처 유지 */
    public void scan(List<DslStatement> statements, BuildContext ctx) {
        execute(new AliasScanContext(statements, ctx, repoMetaRegistry));
    }

    @Override
    public void execute(AliasScanContext ctx) {
        try {
            scanFromStep.execute(ctx);
            scanJoinStep.execute(ctx);
            scanJoinGroupStep.execute(ctx);
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
        }
    }
}