package io.jpm.config.ast;

import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.AutoDDLPolicy;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.ArgumentTokenExtractor;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.DslCommandProcessor;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.SegmentInliner;
import io.jpm.core.jpm_repository.handler.find_repo_meta_handler.FindRepoMetaValidProc;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils.LocalVariableCollector;
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
    public abstract ArgumentTokenExtractor tokenExtractor();
    public abstract DslCommandProcessor commandProcessor();
    public abstract MapParamRegistryImpl mapParamRegistry();
    public abstract SegmentInliner segmentInliner();
    public abstract ErrorTracker errorTracker();
    public abstract FindRepoMetaValidProc findRepoMetaValidProc();

    public abstract LocalVariableCollector localVariableCollector();

    // AppConfig 초기화는 빌드 후 별도 호
}