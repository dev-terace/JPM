package io.jpm.core.m_entity.parse.infra;

import io.jpm.api.MEntity;
import io.jpm.api.MField;
import io.jpm.api.m_field_type.MFieldTypeEnum;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.core.m_entity.parse.domain.policy.MObjectFactoryV2;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.common.utils.LogPrinter;
import io.jpm.core.m_entity.parse.infra.ast.AstMFieldParserV2; // 🚀 V2: Tree API 유틸 사용
import io.jpm.core.m_entity.parse.domain.vo.MEntityInfo;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetadataCacheV3 {

    // 엔티티 이름(ClassName)을 키로 하는 메타데이터 저장소
    public final Map<String, MEntityInfo> entityInfoMap;

    // 파싱된 MField 객체들의 캐시 (빌드 과정 중 재사용)
    public final Map<String, List<MField>> parsedVariablesCache;

    private final RepoMetaRegistry repoMetaRegistry;
    private final EntityRelationRegistry entityRelationRegistry;
    private final ErrorTracker errorTracker;
    private final MObjectFactoryV2 objectFactory;
    public MetadataCacheV3(BuildTimeMetadataCache cache, ErrorTracker errorTracker) {
        this.repoMetaRegistry = cache.getRepoMetaRegistry();
        this.entityRelationRegistry = cache.getEntityRelationRegistry();
        this.entityInfoMap = cache.getEntityInfoMap();
        this.parsedVariablesCache = cache.getParsedVariablesCache();
        this.errorTracker = errorTracker;
        this.objectFactory = new MObjectFactoryV2();
    }



    /**
     * Tree API를 통해 추출된 rawData를 기반으로 캐시를 생성하고 저장합니다.
     */
    public void saveMetadataCache(TypeElement element, List<List<AstMFieldParserV2.Pair>> rawData) {
        MEntity entityAnn = element.getAnnotation(MEntity.class);

        // 테이블명 결정 (어노테이션 값이 없으면 클래스명을 기본값으로 사용 가능)
        String tableName = (entityAnn != null && !entityAnn.name().isEmpty())
                ? entityAnn.name()
                : element.getSimpleName().toString();

        // 필드 객체 생성 및 PK 컬럼 탐색


        PairResult result = processFieldsAndFindPkAndFk(element, rawData);

        MEntityInfo info = new MEntityInfo(tableName, result.getPkColumnName());
        String className = element.getSimpleName().toString();

        // 캐시 업데이트
        entityInfoMap.put(className, info);
        parsedVariablesCache.put(className, result.getVariables());
        repoMetaRegistry.registerTable(className, tableName);
        assert entityAnn != null;
        repoMetaRegistry.registerEntity(element.getClass());



    }

    /**
     * Raw 데이터를 MField 객체로 변환하고 PK 정보를 추출합니다.
     */
    private PairResult processFieldsAndFindPkAndFk(TypeElement element, List<List<AstMFieldParserV2.Pair>> rawData) {
        String pkColumnName = "id"; // 기본값 (PK를 못 찾을 경우 대비)
        List<MField> variables = new ArrayList<>();

        for (List<AstMFieldParserV2.Pair> fieldRawData : rawData) {
            // 🚀 MObjectFactory가 MTreeUtils.Pair를 지원하도록 업데이트되어야 함
            try {
                MField var = objectFactory.createMVariableV2(fieldRawData, errorTracker);

                variables.add(var);

                // 해당 필드가 PK로 설정되어 있다면 컬럼명 저장
                String entityName = String.valueOf(element.getSimpleName());
                if (var.isPrimaryKey()) {
                    entityRelationRegistry.registerPkFieldType(String.valueOf(entityName), var.getType().name());
                    entityRelationRegistry.registerPkFieldName(entityName, pkColumnName);
                } else if (var.getType().equals(MFieldTypeEnum.FK)) {
                    String fieldName = extractFkFieldName(fieldRawData);
                    entityRelationRegistry.registerFk(entityName, fieldName, var.getParentClassName());
                }



            } catch (Exception e) {
                LogPrinter.exceptionInfo(e);
                throw new RuntimeException(e);
            }

        }

        return new PairResult(variables, pkColumnName);
    }

    private String extractFkFieldName(List<AstMFieldParserV2.Pair> fieldRawData) {

        for (AstMFieldParserV2.Pair pair : fieldRawData) {
            if ("fieldName".equals(pair.key)) {
                return pair.value;

            }
        }
        return null;
    }
    /**
     * 내부 데이터 전달용 클래스 (기존 Pair 클래스의 V2 버전)
     */
    private static class PairResult {
        private final List<MField> variables;
        private final String pkColumnName;

        private PairResult(List<MField> variables, String pkColumnName) {
            this.variables = variables;
            this.pkColumnName = pkColumnName;
        }

        public List<MField> getVariables() { return variables; }
        public String getPkColumnName() { return pkColumnName; }
    }
}

