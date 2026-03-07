package io.jpm.config.ast;

import io.jpm.api.MField;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.exception.ErrorTrackerImpl;
import io.jpm.common.exception.cache.domain.SourceLocationCache;
import io.jpm.common.exception.cache.infra.SourceLocationCacheImpl;
import io.jpm.core.jpm_repository.parse.infra.MapParamRegistry;
import io.jpm.core.jpm_repository.parse.infra.RepoMetaRegistryImpl;
import io.jpm.core.jpm_repository.parse.infra.RepoRelationRegistryImpl;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.core.jpm_repository.parse.domain.cache.EntityRelationRegistry;
import io.jpm.core.m_entity.parse.domain.vo.MEntityInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildTimeMetadataCache {

    // 철저하게 해당 인스턴스(빌드 세션)에서만 유지되는 로컬 상태
    private final Map<String, MEntityInfo> entityInfoMap = new HashMap<>();
    private final Map<String, List<MField>> parsedVariablesCache = new HashMap<>();

    //핸들러 캐시
    private final Map<Class<?>, Object> store = new HashMap<>();

    // 의존성 주입을 위한 final 변수
    private final RepoMetaRegistry repoMetaRegistry;
    private final EntityRelationRegistry relationRegistry;
    private final SourceLocationCache sourceLocationCache;


    private final MapParamRegistry mapParamRegistry;
    // 생성자에서 의존성 초기화 (AppConfig 사용)
    public BuildTimeMetadataCache() {
        this.repoMetaRegistry = new RepoMetaRegistryImpl();
        this.relationRegistry = new RepoRelationRegistryImpl();
        this.sourceLocationCache = new SourceLocationCacheImpl();
        this.mapParamRegistry = new MapParamRegistry();
    }


    public MapParamRegistry getMapParamRegistry() {
        return mapParamRegistry;
    }



    // 외부에서 로컬 캐시 데이터가 필요할 때를 위한 Getter 제공
    public Map<String, MEntityInfo> getEntityInfoMap() {
        return entityInfoMap;
    }

    public Map<String, List<MField>> getParsedVariablesCache() {
        return parsedVariablesCache;
    }




    public EntityRelationRegistry getRelationRegistry() {
        return relationRegistry;
    }



    public SourceLocationCache getSourceLocationCache() {
        return sourceLocationCache;
    }

    public RepoMetaRegistry getRepoMetaRegistry() {
        return repoMetaRegistry;
    }

    public EntityRelationRegistry getEntityRelationRegistry() {
        return relationRegistry;
    }
}