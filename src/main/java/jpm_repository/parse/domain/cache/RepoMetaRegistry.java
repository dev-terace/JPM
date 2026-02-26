package jpm_repository.parse.domain.cache;

import jpm_repository.parse.domain.vo.EntityMeta;
import utils.Pair;

import java.util.List;

public interface RepoMetaRegistry {

    // 핵심 등록 메서드 (V1/V2 통합)
    <T extends Pair> void  register(String entityName, List<List<T>> rawMeta);
    public void registerEntityAlias(String simpleName, String aliasName);
    // 테이블 및 세그먼트 등록
    void registerTable(String entityName, String tableName);

    void registerSegmentPath(String repoName, String fieldVarName, String segmentFqcn);
    String getSegmentPath(String repoName, String fieldVarName);


    void registerEntity(Class<?> entityClass);
    // 조회 메서드
    EntityMeta getEntityMeta(String entityName);
    String getTable(String entityName);


    Class<?> getEntityClass(String entityName);







}
