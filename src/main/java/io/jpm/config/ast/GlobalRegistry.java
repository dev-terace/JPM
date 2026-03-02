package io.jpm.config.ast;

import io.jpm.config.AppConfig;
import io.jpm.config.AutoDDLPolicy;

import io.jpm.common.utils.LogPrinter;
import javax.annotation.processing.ProcessingEnvironment;
import java.util.Map;

/**
 * [JPM] 컴파일 타임 전역 설정 정보
 * 레지스트리는 BuildTimeMetadataCache가 담당하므로, 여기서는 옵션값만 관리합니다.
 */
public class GlobalRegistry {

    private final AutoDDLPolicy autoPolicy;
    private final String dbType;
    private final Map<String, String> options;

    public GlobalRegistry(ProcessingEnvironment processingEnv) {
        this.options = processingEnv.getOptions();

        // 1. auto 모드 설정 (DISABLED, CREATE, UPDATE 등)
        this.autoPolicy = initAutoPolicy();

        // 2. DB 타입 설정
        this.dbType = options.getOrDefault("dbType", "MYSQL").toUpperCase();

        // 3. SQL Dialect 초기화 (AppConfig 내부의 Dialect 세팅 수행)
        AppConfig.sqlDialectInit(options);

        LogPrinter.info("🚀 [GlobalRegistry] Initialized - Policy: " + autoPolicy + ", DB: " + dbType);
    }

    private AutoDDLPolicy initAutoPolicy() {
        String autoStr = options.getOrDefault("auto", "DISABLED").toUpperCase();
        try {
            return AutoDDLPolicy.valueOf(autoStr);
        } catch (IllegalArgumentException e) {
            LogPrinter.warn("⚠️ [JPM] 알 수 없는 auto 모드입니다 ('" + autoStr + "'). DISABLED로 설정합니다.");
            return AutoDDLPolicy.DISABLED;
        }
    }

    // --- Getters ---
    public boolean isEnabled() {
        return autoPolicy != AutoDDLPolicy.DISABLED;
    }

    public AutoDDLPolicy getAutoPolicy() {
        return autoPolicy;
    }

    public String getDbType() {
        return dbType;
    }

    public String getOption(String key, String defaultValue) {
        return options.getOrDefault(key, defaultValue);
    }

    public Map<String, String> getOptions() {
        return options;
    }
}