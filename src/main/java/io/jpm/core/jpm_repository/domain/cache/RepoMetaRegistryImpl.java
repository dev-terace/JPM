package io.jpm.core.jpm_repository.domain.cache;

import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.common.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoMetaRegistryImpl implements RepoMetaRegistry {
    // Key: EntityClassName (예: MEntity3), Value: FieldMetadata Map (Key: Java 필드명, Value: DB 컬럼명)
    private final Map<String, EntityMeta> registry = new HashMap<>();

    // Key: Repository 파일의 절대 경로 (repoPath)
    // Value: Segment 파일의 절대 경로 (segmentPath)
    private final Map<String, Map<String, String>> segmentPathMap = new HashMap<>();


    private final Map<String, String> entityAliasMap = new HashMap<>();


    private final Map<String, Class<?>> classMap = new HashMap<>();
    // 클래스명 -> 테이블명 매핑 (선택 사항, 필요시 사용)
    private final Map<String, String> tableRegistry = new HashMap<>();

    private final Map<String, String> columnToField = new HashMap<>();

    public void addMapping(String fieldName, String columnName) {
        columnToField.put(columnName, fieldName); // 역방향 추가
    }


    public String getFieldType(String entityName, String fieldName) {
        if (entityName == null || entityName.isEmpty()) return null;
        EntityMeta entityMeta = registry.get(entityName);
        System.out.println("[getFieldType] entityName=" + entityName + " fieldName=" + fieldName + " meta=" + (entityMeta != null ? "found" : "null"));
        if (entityMeta == null) return null;
        return entityMeta.getFieldType(fieldName);
    }

    public List<String> getSegmentVarNames(String entityName) {
        // 1. 해당 entityName에 맞는 안쪽 Map을 가져옴
        Map<String, String> innerMap = segmentPathMap.get(entityName);

        // 2. 만약 해당 엔티티 정보가 아예 없거나 안쪽 Map이 비어있을 경우 로그 출력
        if (innerMap == null || innerMap.isEmpty()) {
            LogPrinter.info("[SEGMENT_NOT_FOUND] No variable names found for entity: " + entityName);
            return new ArrayList<>(); // 빈 리스트 반환 (NullPointerException 방지)
        }

        // 3. 안쪽 Map의 모든 Key(변수명)를 List로 반환
        return new ArrayList<>(innerMap.keySet());
    }


    //condition 에서 체크, inner/left에서 체크
    //1.key: pk field, fieldType Map<EntityName, pkFieldType> pk 저장용
    //2. fk: Map<EntityName, Map<fieldType, parentEntity> >

    //mqRepoParser에서 탐색 시 검증
    //field 값 case FK 를 탐

    //case fk 일 시 Entity::fieldName 임 -> 분리
    //2.에서 Map<FieldType, parentEntity>>을 가져옴
    //fieldType에서 parentEntity를 추출함
    //1.에서 entityName을 조회해서 pkFieldType을 가져옴
    //Expression Arg와 pkFieldType을 비교함

    // MParserUtils의 결과를 Registry에 등록

    public <T extends Pair> void register(String entityName, List<List<T>> rawMeta) {


        String tableName = getTable(entityName);
        EntityMeta entityMeta = new EntityMeta(tableName, this);


        for (List<T> fieldInfo : rawMeta) {
            String fieldName = null;  // Java 변수명 (예: level, isActive)
            String columnName = null; // DB 컬럼명 (예: user_level, is_active)
            String typeName = null;
            String fkFieldName = null;

            for (T pair : fieldInfo) {
                // 1. 자바 변수명 (MParserUtils가 "fieldName"으로 넘겨준다고 가정)
                if ("fieldName".equals(pair.getKey())) {
                    System.out.println("[EntityMetaRegistry] fieldName=" + pair.getValue());
                    fieldName = pair.getValue();

                }

                // 2. DB 컬럼명 (MField.builder().name("...") 또는 .column("...") 형태 모두 지원)
                if ("name".equals(pair.getKey()) || "column".equals(pair.getKey())) {
                    columnName = pair.getValue().replace("\"", "").replace("'", "");
                }

                if ("type".equals(pair.getKey())) typeName = pair.getValue();
            }

            // 만약 .name() 이나 .column() 지정이 없으면 자바 필드명을 그대로 DB 컬럼명으로 사용
            if (columnName == null || columnName.trim().isEmpty()) {
                columnName = fieldName;
            }

            if (fieldName != null) {
                entityMeta.addMapping(fieldName, columnName);
                if (typeName != null) entityMeta.addTypeMapping(fieldName, typeName);

                System.out.println("[register] tableName=" + tableName + ", columnName=" + columnName + ", typeName=" + typeName);


            }
        }

        registry.put(entityName, entityMeta);
    }


    // --- (옵션) 테이블명 관련 유틸 ---
    public void registerTable(String entityName, String tableName) {
        tableRegistry.put(entityName, tableName.replace("\"", "").replace("'", ""));
    }

    @Override
    public void registerSegmentPath(String repoName, String fieldVarName, String segmentFqcn) {
        segmentPathMap
                .computeIfAbsent(repoName, k -> new HashMap<>())
                .put(fieldVarName, segmentFqcn);
    }

    @Override
    public String getSegmentPath(String repoName, String fieldVarName) {
        Map<String, String> innerMap = segmentPathMap.get(repoName);
        if (innerMap == null) return null;
        return innerMap.get(fieldVarName);
    }

    public String getTable(String entityName) {
        return tableRegistry.getOrDefault(entityName, entityName);
    }


    public void registerEntity(Class<?> entityClass) {
        // entityClass.getSimpleName() 은 "UserEntity" 와 같은 짧은 이름을 반환합니다.
        classMap.put(entityClass.getSimpleName(), entityClass);

       /* LogPrinter.info("[RepoMetaRegistry] entityClassName=" + entityClass + " entityClass=" + entityClass);*/

        // EntityMeta 객체 생성 및 저장 로직도 여기에 함께 구현...
    }

    public Class<?> getEntityClass(String entityName) {
        return classMap.get(entityName);
    }


    @Override
    public void registerEntityAlias(String simpleName, String aliasName) {
        entityAliasMap.put(simpleName, aliasName);
    }

    @Override
    public EntityMeta getEntityMeta(String entityName) {


        // 1️⃣ alias나 필드 포함된 경우 마지막 토큰만 추출
        // ex)
        // o1.orderEntity        -> orderEntity
        // o1.orderEntity.id     -> id (이건 방어용)
        // com.test.OrderEntity  -> OrderEntity

        String normalized = "";
        try {


/*            LogPrinter.info("[RepoMetaRegistry] entityName=" + entityName);*/

            normalized = entityName;
            int lastDot = normalized.lastIndexOf('.');
            if (lastDot != -1) {
                normalized = normalized.substring(lastDot + 1);
            }

            // 2️⃣ registry 직접 조회
            EntityMeta meta = registry.get(normalized);
            if (meta != null) return meta;

            // 3️⃣ simpleClassName → alias 매핑 조회
            String alias = entityAliasMap.get(normalized);
            if (alias != null) {
                EntityMeta aliasMeta = registry.get(alias);
                if (aliasMeta != null) return aliasMeta;
            }

        } catch (Exception e) {
            System.err.println("=== ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                System.err.println("  at " + ste);
            }
            throw new NullPointerException(
                    "getEntityMeta not found for entityName=" + entityName +
                            " (normalized=" + normalized + ")"
            );

        }
        return null;

    }



}