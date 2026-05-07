package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.alias_pre_scanner;

import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;

import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context.AliasScanContext;




public class AliasPreScannerSteps implements Step<AliasScanContext> {

    private final ScanFromStep      scanFromStep      = new ScanFromStep();
    private final ScanJoinStep      scanJoinStep      = new ScanJoinStep();
    private final ScanJoinGroupStep scanJoinGroupStep = new ScanJoinGroupStep();

    private final RepoMetaRegistry repoMetaRegistry;

    public AliasPreScannerSteps(RepoMetaRegistry repoMetaRegistry) {
        this.repoMetaRegistry = repoMetaRegistry;
    }

    /** SqlMapperBinderImplV2 에서 직접 호출하는 기존 시그니처 유지 */


    @Override
    public void execute(AliasScanContext aliasCtx) {


        try {
            scanFromStep.execute(aliasCtx);
            scanJoinStep.execute(aliasCtx);
            scanJoinGroupStep.execute(aliasCtx);
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
        }
    }


}