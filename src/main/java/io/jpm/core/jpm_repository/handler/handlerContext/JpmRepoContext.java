package io.jpm.core.jpm_repository.handler.handlerContext;

import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.exception.ErrorTrackerImpl;
import io.jpm.common.exception.config.JpmChainExtractorScanner;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.Context;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.core.jpm_repository.domain.model.RepoMeta;

import javax.annotation.processing.RoundEnvironment;
import java.util.List;

public class JpmRepoContext implements Context {

    private List<RepoMeta> repoMetas;
    private final RoundEnvironment roundEnv;
    private final BuildTimeMetadataCache cache;
    private final AstContext context;
    private final GlobalRegistry globalRegistry;
    private final JpmChainExtractorScanner jpmChainExtractorScanner;
    private final ErrorTracker errorTracker;

    public JpmRepoContext(RoundEnvironment roundEnv, BuildTimeMetadataCache cache, AstContext context, GlobalRegistry globalRegistry) {
        this.roundEnv = roundEnv;
        this.cache = cache;
        this.context = context;
        this.globalRegistry = globalRegistry;
        this.jpmChainExtractorScanner = new JpmChainExtractorScanner(context.getJpmToolbox());
        this.errorTracker = globalRegistry.errorTracker();
    }


    public JpmChainExtractorScanner getJpmChainExtractorScanner() {
        return jpmChainExtractorScanner;
    }

    public List<RepoMeta> getRepoMetas() {
        return repoMetas;
    }

    public void setRepoMetas(List<RepoMeta> repoMetas) {
        this.repoMetas = repoMetas;
    }


    public RoundEnvironment getRoundEnv() {
        return roundEnv;
    }

    public BuildTimeMetadataCache getCache() {
        return cache;
    }

    public AstContext getContext() {
        return context;
    }

    public GlobalRegistry getGlobalRegistry() {
        return globalRegistry;
    }

    public ErrorTracker getErrorTracker() {
        return errorTracker;
    }


}