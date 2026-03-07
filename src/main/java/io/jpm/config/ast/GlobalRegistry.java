package io.jpm.config.ast;

import com.sun.source.util.Trees;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.AppConfig;
import io.jpm.config.AutoDDLPolicy;
import io.jpm.core.jpm_repository.parse.infra.MapParamRegistry;
import io.jpm.core.jpm_repository.parse.infra.ast.argument_token_extractor.AstArgumentTokenExtractorV2;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_dsl_command_proc.AstDslCommandProcV2;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_segment_inliner.AstSegmentInlinerV3;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public abstract class GlobalRegistry {

    public abstract Map<String, String> options();

    @Value.Derived
    public AutoDDLPolicy autoPolicy() {
        String autoStr = options().getOrDefault("auto", "DISABLED").toUpperCase();
        try {
            return AutoDDLPolicy.valueOf(autoStr);
        } catch (IllegalArgumentException e) {
            LogPrinter.warn("⚠️ [JPM] 알 수 없는 auto 모드입니다 ('" + autoStr + "'). DISABLED로 설정합니다.");
            return AutoDDLPolicy.DISABLED;
        }
    }

    @Value.Derived
    public String dbType() {
        return options().getOrDefault("dbType", "MYSQL").toUpperCase();
    }

    // 기존 필드들
    public abstract AstArgumentTokenExtractorV2 tokenExtractor();
    public abstract AstDslCommandProcV2 commandProcessor();
    public abstract MapParamRegistry mapParamRegistry();
    public abstract AstSegmentInlinerV3 segmentInliner();
    public abstract ErrorTracker errorTracker();


    // AppConfig 초기화는 빌드 후 별도 호
}