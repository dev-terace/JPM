package io.jpm.core.jpm_repository.domain.cache.interfaces;

import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.common.utils.Pair;

import java.util.List;

public interface RepoMetaRegistry {

    // 핵심 등록 메서드 (V1/V2 통합)
    <T extends Pair> void  register(String entityName, List<List<T>> rawMeta);
    void registerEntityAlias(String simpleName, String aliasName);
    // 테이블 및 세그먼트 등록
    void registerTable(String entityName, String tableName);

    void registerSegmentPath(String repoName, String fieldVarName, String segmentFqcn);
    String getSegmentPath(String repoName, String fieldVarName);


    void registerEntity(String simpleName, String qualifiedName);
    // 조회 메서드
    EntityMeta getEntityMeta(String entityName);
    String getTable(String entityName);



    String getEntityPath(String entityName);
    List<String> getSegmentVarNames(String entityName);






}
