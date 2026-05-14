package io.jpm.core.jpm_repository.domain.cache;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jpm.common.utils.CustomLogger;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.common.utils.Pair;
import org.gradle.internal.impldep.com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.gradle.internal.impldep.com.fasterxml.jackson.annotation.JsonIgnore;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RepoMetaRegistryImpl implements RepoMetaRegistry {
    // Key: EntityClassName (예: MEntity3), Value: FieldMetadata Map (Key: Java 필드명, Value: DB 컬럼명)
    private final Map<String, EntityMeta> registry = new HashMap<>();

    // Key: Repository 파일의 절대 경로 (repoPath)
    // Value: Segment 파일의 절대 경로 (segmentPath)
    private final Map<String, Map<String, String>> segmentPathMap = new HashMap<>();


    private final Map<String, String> entityAliasMap = new HashMap<>();


    private final Map<String, String> classPathMap = new HashMap<>();
    // 클래스명 -> 테이블명 매핑 (선택 사항, 필요시 사용)
    private final Map<String, String> tableRegistry = new HashMap<>();

    private final Map<String, String> columnToField = new HashMap<>();


    @JsonIgnore
    private static final CustomLogger log = CustomLogger.getLogger(RepoMetaRegistryImpl.class);

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

            return new ArrayList<>(); // 빈 리스트 반환 (NullPointerException 방지)
        }

        // 3. 안쪽 Map의 모든 Key(변수명)를 List로 반환
        return new ArrayList<>(innerMap.keySet());
    }



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


    public void registerEntity(String simpleName, String qualifiedName) {
        // entityClass.getSimpleName() 은 "UserEntity" 와 같은 짧은 이름을 반환합니다.


        classPathMap.put(simpleName, qualifiedName);


        // EntityMeta 객체 생성 및  저장 로직도 여기에 함께 구현...
    }

    public String getEntityPath(String entityName) {


        return classPathMap.get(entityName);
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
            log.error(e.getMessage());

            throw new NullPointerException(
                    "getEntityMeta not found for entityName=" + entityName +
                            " (normalized=" + normalized + ")"
            );

        }
        return null;

    }

    public static RepoMetaRegistryImpl fromJson(InputStream is) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> root = mapper.readValue(
                    is,
                    new TypeReference<Map<String, Object>>() {}
            );

            RepoMetaRegistryImpl registry = new RepoMetaRegistryImpl();

            Map<?, ?> entities = (Map<?, ?>) root.get("registry");

            if (entities != null) {
                for (Map.Entry<?, ?> entry : entities.entrySet()) {
                    String entityName = String.valueOf(entry.getKey());
                    Map<?, ?> entityJson = (Map<?, ?>) entry.getValue();

                    String table = String.valueOf(entityJson.get("tableName"));
                    EntityMeta meta = new EntityMeta(table, registry);

                    // fieldToColumn
                    Map<?, ?> rawFields = (Map<?, ?>) entityJson.get("fieldToColumn");
                    if (rawFields != null) {
                        for (Map.Entry<?, ?> f : rawFields.entrySet()) {
                            meta.addMapping(
                                    String.valueOf(f.getKey()),
                                    String.valueOf(f.getValue())
                            );
                        }
                    }

                    // fieldToType
                    Map<?, ?> rawTypes = (Map<?, ?>) entityJson.get("fieldToType");
                    if (rawTypes != null) {
                        for (Map.Entry<?, ?> t : rawTypes.entrySet()) {
                            meta.addTypeMapping(
                                    String.valueOf(t.getKey()),
                                    String.valueOf(t.getValue())
                            );
                        }
                    }

                    registry.registry.put(entityName, meta);
                }
            }

            // tableRegistry
            Map<?, ?> tables = (Map<?, ?>) root.get("tableRegistry");
            if (tables != null) {
                for (Map.Entry<?, ?> e : tables.entrySet()) {
                    registry.tableRegistry.put(
                            String.valueOf(e.getKey()),
                            String.valueOf(e.getValue())
                    );
                }
            }

            // entityAliasMap
            Map<?, ?> aliases = (Map<?, ?>) root.get("entityAliasMap");
            if (aliases != null) {
                for (Map.Entry<?, ?> e : aliases.entrySet()) {
                    registry.entityAliasMap.put(
                            String.valueOf(e.getKey()),
                            String.valueOf(e.getValue())
                    );
                }
            }

            // classPathMap
            Map<?, ?> classPaths = (Map<?, ?>) root.get("classPathMap");
            if (classPaths != null) {
                for (Map.Entry<?, ?> e : classPaths.entrySet()) {
                    registry.classPathMap.put(
                            String.valueOf(e.getKey()),
                            String.valueOf(e.getValue())
                    );
                }
            }

            return registry;

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }



}