package io.jpm.core.m_entity.parse.infra.ast;



import io.jpm.api.MEntity;
import com.sun.source.util.Trees;
import io.jpm.config.AppConfig;
import io.jpm.api.MField;
import io.jpm.core.m_entity.generator.domain.vo.DDLTableMetadata;
import io.jpm.core.m_entity.parse.domain.ast.DDLMetaDataLoader;
import io.jpm.core.m_entity.parse.domain.vo.MEntityInfo;
import io.jpm.core.m_entity.parse.domain.policy.TableMetadataFactory;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.core.m_entity.parse.infra.MetadataCacheV2;
import io.jpm.common.utils.LogPrinter;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class DDLMetaDataLoaderAstImplV2 implements DDLMetaDataLoader {

    private final RoundEnvironment roundEnv;
    private final Messager messager;
    private final Trees trees; // 🚀 추가: Tree API의 핵심


    private final RepoMetaRegistry repoMetaRegistry;


    public DDLMetaDataLoaderAstImplV2(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, RepoMetaRegistry repoMetaRegistry) {
        this.roundEnv = roundEnv;
        this.messager = processingEnv.getMessager();
        // 🚀 Trees 인스턴스 초기화
        this.trees = Trees.instance(processingEnv);
        this.repoMetaRegistry = repoMetaRegistry;
    }

    @Override
    public List<DDLTableMetadata> load(RoundEnvironment ignored) {

        List<DDLTableMetadata> tables = new ArrayList<>();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MEntity.class);

        // --- Step 1. 모든 엔티티 사전 스캔 (Tree API 활용) ---
        for (Element element : elements) {
            if (element instanceof TypeElement) {

                scanEntity((TypeElement) element);
            }
        }

        // --- Step 2. 메타데이터 생성 (기존 로직 유지) ---
        for (String className : MetadataCacheV2.parsedVariablesCache.keySet()) {
            try {
                MEntityInfo currentEntityInfo = MetadataCacheV2.entityInfoMap.get(className);
                // 여기서 꺼내는 데이터는 scanEntity에서 Tree API로 채운 데이터입니다.
                List<MField> fields = MetadataCacheV2.parsedVariablesCache.get(className);

                DDLTableMetadata table = TableMetadataFactory.create(fields, currentEntityInfo, MetadataCacheV2.entityInfoMap);

                if (table != null) {
                    tables.add(table);
                }
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "DDL Generation Failed for " + className + ": " + e.getMessage());
            }
        }
        return tables;
    }

    /**
     * Tree API를 사용하여 클래스 내부의 MField 정보를 직접 추출합니다.
     */
    private void scanEntity(TypeElement classElement) {
        try {
            List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());
            List<List<AstMFieldParserV2.Pair>> allFieldsRawData = new ArrayList<>();

            for (VariableElement field : fields) {
                if (field.asType().toString().contains("MField")) {
                    List<AstMFieldParserV2.Pair> fieldInfo = AstMFieldParserV2.extractFieldInfo(field, trees);
                    if (!fieldInfo.isEmpty()) {
                        allFieldsRawData.add(fieldInfo);
                    }
                }
            }

            MetadataCacheV2.saveMetadataCache(classElement, allFieldsRawData);

            String simpleName = classElement.getSimpleName().toString(); // "OrderEntity"
            String entityName = extractEntityName(classElement);         // name 있으면 그 값, 없으면 simpleName

            repoMetaRegistry.register(entityName, allFieldsRawData);

            // simpleName과 entityName이 다를 때만 alias 등록
            // ex) simpleName="OrderEntity", entityName="orders_alias"
            if (!simpleName.equals(entityName)) {
                repoMetaRegistry.registerEntityAlias(simpleName, entityName);

            }

        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "AST Parsing failed for " + classElement.getSimpleName() + ": " + e.getMessage());
        }
    }

    private  String extractEntityName(TypeElement classElement) {
        for (AnnotationMirror mirror : classElement.getAnnotationMirrors()) {
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                    : mirror.getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().toString().equals("name")) {
                    String value = entry.getValue().getValue().toString().trim();
                    if (!value.isEmpty()) return value;
                }
            }
        }
        return classElement.getSimpleName().toString();
    }
}
