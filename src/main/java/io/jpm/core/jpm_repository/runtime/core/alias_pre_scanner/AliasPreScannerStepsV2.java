package io.jpm.core.jpm_repository.runtime.core.alias_pre_scanner;

import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;


import io.jpm.core.jpm_repository.runtime.context.AliasScanContextV2;

public class AliasPreScannerStepsV2 implements Step<AliasScanContextV2> {

    private final ScanFromStepV2      scanFromStep      = new ScanFromStepV2();
    private final ScanJoinStepV2 scanJoinStepV2 = new ScanJoinStepV2();
    private final ScanJoinGroupStepV2 scanJoinGroupStep = new ScanJoinGroupStepV2();

    @Override
    public void execute(AliasScanContextV2 aliasCtx) {

        try {

            scanFromStep.execute(aliasCtx);
            scanJoinStepV2.execute(aliasCtx);
            scanJoinGroupStep.execute(aliasCtx);

        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
        }
    }


}