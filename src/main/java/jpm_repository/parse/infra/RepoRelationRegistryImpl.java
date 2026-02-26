package jpm_repository.parse.infra;

import config.AppConfig;
import jpm_repository.parse.domain.cache.RepoMetaRegistry;
import jpm_repository.parse.domain.cache.EntityRelationRegistry;
import utils.LogPrinter;

import java.util.HashMap;
import java.util.Map;

// 새로운 클래스
public class RepoRelationRegistryImpl implements EntityRelationRegistry {

    // 1. PK: Map<EntityName, pkFieldType>
    private final Map<String, String> pkFieldTypeMap = new HashMap<>();
    private final Map<String, String> pkFieldNameMap = new HashMap<>();
    private final RepoMetaRegistry repoMetaRegistry = AppConfig.getEntityMetaRegistry();
    // 2. FK: Map<EntityName, Map<fkFieldName, parentEntityName>>
    private final Map<String, Map<String, String>> fkMap = new HashMap<>();

    public void registerPkFieldType(String entityName, String pkFieldType) {
        pkFieldTypeMap.put(entityName, pkFieldType);
    }
    public void registerPkFieldName(String entityName, String pkFieldName) {
        pkFieldNameMap.put(entityName, pkFieldName);
    }

    public void registerFk(String entityName, String fkFieldName, String parentEntityName) {
        fkMap.computeIfAbsent(entityName, k -> new HashMap<>())
                .put(fkFieldName, parentEntityName);

        LogPrinter.info("[registerFk] entityName=" + entityName
                + ", fkFieldName=" + fkFieldName
                + ", parentEntityName=" + parentEntityName);
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

}
