package io.jpm.core.jpm_repository.domain.cache;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.common.utils.LogPrinter;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// 새로운 클래스


@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RepoRelationRegistryImpl implements EntityRelationRegistry {

    // 1. PK: Map<EntityName, pkFieldType>
    @JsonProperty
    private final Map<String, String> pkFieldTypeMap = new HashMap<>();

    @JsonProperty
    private final Map<String, String> pkFieldNameMap = new HashMap<>();

    // 2. FK: Map<EntityName, Map<fkFieldName, parentEntityName>>
    @JsonProperty
    private final Map<String, Map<String, String>> fkMap = new HashMap<>();

    public void  registerPkFieldType(String entityName, String pkFieldType) {
        pkFieldTypeMap.put(entityName, pkFieldType);
    }

    public void registerPkFieldName(String entityName, String pkFieldName) {
        pkFieldNameMap.put(entityName, pkFieldName);
    }

    public void registerFk(String entityName, String fkFieldName, String parentEntityName) {
        fkMap.computeIfAbsent(entityName, k -> new HashMap<>())
                .put(fkFieldName, parentEntityName);

    }

    public String getPkFieldType(String entityName) {
        return pkFieldTypeMap.get(entityName);
    }

    public String getPkFieldName(String entityName) {
        return pkFieldNameMap.get(entityName);
    }

    /**
     * FK 필드 표현식 "Entity::fieldName" 을 파싱해서 parentEntity의 PK 타입과 비교
     */
    public String resolveFkType(String entityName, String fkFieldName) {

        if(entityName.contains("."))
        {
            entityName = entityName.split("\\.")[1];
        }
        Map<String, String> fkEntry = fkMap.get(entityName);
        if (fkEntry == null) {
            throw new RuntimeException("FK 정보 없음: entityName=" + entityName);
        }

        String parentEntityName = fkEntry.get(fkFieldName);
        if (parentEntityName == null) {
            throw new RuntimeException("FK 필드 없음: entityName=" + entityName + ", fkFieldName=" + fkFieldName);
        }

        String pkFieldType = pkFieldTypeMap.get(parentEntityName);
        if (pkFieldType == null) {
            throw new RuntimeException("부모 엔티티 PK 타입 없음: parentEntityName=" + parentEntityName);
        }

        return pkFieldType;
    }


    public  static EntityRelationRegistry fromJson(InputStream is) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> root = mapper.readValue(
                    is,
                    new TypeReference<Map<String, Object>>() {}
            );

            EntityRelationRegistry registry = new RepoRelationRegistryImpl();

            // pkFieldTypeMap
            Map<?, ?> pkTypes = (Map<?, ?>) root.get("pkFieldTypeMap");
            if (pkTypes != null) {
                for (Map.Entry<?, ?> e : pkTypes.entrySet()) {
                    registry.registerPkFieldType(
                            String.valueOf(e.getKey()),
                            String.valueOf(e.getValue())
                    );
                }
            }

            // pkFieldNameMap
            Map<?, ?> pkNames = (Map<?, ?>) root.get("pkFieldNameMap");
            if (pkNames != null) {
                for (Map.Entry<?, ?> e : pkNames.entrySet()) {
                    registry.registerPkFieldName(
                            String.valueOf(e.getKey()),
                            String.valueOf(e.getValue())
                    );
                }
            }

            // fkMap
            Map<?, ?> fks = (Map<?, ?>) root.get("fkMap");
            if (fks != null) {
                for (Map.Entry<?, ?> entityEntry : fks.entrySet()) {
                    String entityName = String.valueOf(entityEntry.getKey());
                    Map<?, ?> fkFields = (Map<?, ?>) entityEntry.getValue();
                    if (fkFields != null) {
                        for (Map.Entry<?, ?> fkEntry : fkFields.entrySet()) {
                            registry.registerFk(
                                    entityName,
                                    String.valueOf(fkEntry.getKey()),
                                    String.valueOf(fkEntry.getValue())
                            );
                        }
                    }
                }
            }

            return registry;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // pkFieldTypeMap
        sb.append("  \"pkFieldTypeMap\": {");
        sb.append(mapToJson(pkFieldTypeMap));
        sb.append("},\n");

        // pkFieldNameMap
        sb.append("  \"pkFieldNameMap\": {");
        sb.append(mapToJson(pkFieldNameMap));
        sb.append("},\n");

        // fkMap
        sb.append("  \"fkMap\": {\n");
        boolean firstEntity = true;
        for (Map.Entry<String, Map<String, String>> entry : fkMap.entrySet()) {
            if (!firstEntity) sb.append(",\n");
            sb.append("    \"").append(entry.getKey()).append("\": {");
            sb.append(mapToJson(entry.getValue()));
            sb.append("}");
            firstEntity = false;
        }
        sb.append("\n  }\n");

        sb.append("}");
        return sb.toString();
    }

    private String mapToJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (!first) sb.append(", ");
            sb.append("\"").append(e.getKey()).append("\": \"").append(e.getValue()).append("\"");
            first = false;
        }
        return sb.toString();
    }


    @Override
    public String toString() {
        return "RepoRelationRegistryImpl{" +
                "\n  pkFieldTypeMap=" + pkFieldTypeMap +
                ",\n  pkFieldNameMap=" + pkFieldNameMap +
                ",\n  fkMap=" + fkMap +
                "\n}";
    }

}
