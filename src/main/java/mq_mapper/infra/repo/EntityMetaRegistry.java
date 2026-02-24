package mq_mapper.infra.repo;

import mq_mapper.domain.vo.EntityMeta;

import java.util.List;

public interface EntityMetaRegistry {

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




    // 필드 타입 조회 유틸
    default String getFieldType(String entityName, String fieldName) {
        EntityMeta meta = getEntityMeta(entityName);
        return (meta != null) ? meta.getFieldType(fieldName) : null;
    }


}
