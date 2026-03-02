package io.jpm.core.m_entity.parse.deprecated;

import io.jpm.api.MEntity;
import com.github.javaparser.utils.Log;
import io.jpm.core.m_entity.parse.domain.ast.DDLMetaDataLoader;
import io.jpm.core.m_entity.parse.domain.policy.TableMetadataFactory;
import io.jpm.common.utils.MParserUtils;
import io.jpm.api.MField;
import io.jpm.core.m_entity.parse.infra.deprecated.AnnotationUtil; // 파일 경로 얻는 유틸 (기존 유지)
import io.jpm.core.m_entity.generator.domain.vo.DDLTableMetadata;
import io.jpm.core.m_entity.parse.domain.vo.MEntityInfo;
import io.jpm.core.m_entity.parse.infra.deprecated.MetadataCache;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.*;


@Deprecated
public class AnnotationDDLMetaDataLoader implements DDLMetaDataLoader {


    private final RoundEnvironment roundEnv;
    private final Messager messager;
    Map<String, String> options;


    public AnnotationDDLMetaDataLoader(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {

        this.roundEnv = roundEnv;
        this.messager = processingEnv.getMessager();
        this.options = processingEnv.getOptions();
    }



    @Override
    public List<DDLTableMetadata> load(RoundEnvironment ignored) {

        List<DDLTableMetadata> tables = new ArrayList<>();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MEntity.class);

        // --- Step 1. 모든 엔티티 사전 스캔 (Parsing & Info Collecting) ---
        for (Element element : elements) {
            if (element instanceof TypeElement) {
                scanEntity((TypeElement) element);
            }
        }

        // --- Step 2. 메타데이터 생성 (Linking) ---


        for (String className : MetadataCache.parsedVariablesCache.keySet()) {


            try {
                MEntityInfo currentEntityInfo = MetadataCache.entityInfoMap.get(className);
                List<MField> fields = MetadataCache.parsedVariablesCache.get(className);

                Log.info(currentEntityInfo.toString() + fields.toString());
                DDLTableMetadata table =  TableMetadataFactory.create(fields, currentEntityInfo, MetadataCache.entityInfoMap);/*buildTableMetadata(className);*/

                if (table != null) {
                    tables.add(table);
                }
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "DDL Generation Failed for " + className + ": " + e.getMessage());

            }
        }

        return tables;
    }

    // --- 내부 로직 ---

    /**
     * 1. 소스 코드를 파싱해서 MVariable 리스트를 만들고
     * 2. PK 정보와 테이블 이름을 entityInfoMap에 저장합니다.
     */


    private void scanEntity(TypeElement element) {
        try {
            String className = element.getSimpleName().toString();


            // 🚨 [수정] AnnotationUtil이 TypeElement(클래스)를 처리 못하고 에러를 뱉으므로,
            // 클래스 내부의 첫 번째 필드를 찾아서 대신 넘겨줍니다. (같은 파일이므로 경로 동일)
            VariableElement firstField = javax.lang.model.util.ElementFilter.fieldsIn(element.getEnclosedElements())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("[AnnotationMetadataLoader: scanEntity] Entity must have at least one field: " + className));

            // 클래스(element) 대신 필드(firstField)를 넘김
            String filePath = AnnotationUtil.getRelativePath(firstField, false);

            // 2. 파싱 실행 (MParserUtils -> Pairs -> MVariable)
            List<List<MParserUtils.Pair>> rawDataList = MParserUtils.execute(filePath);

            MetadataCache.saveMetadataCache(element, rawDataList);



        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING, "Parsing skipped for " + element.getSimpleName() + ": " + e.getMessage());


        }
    }




}