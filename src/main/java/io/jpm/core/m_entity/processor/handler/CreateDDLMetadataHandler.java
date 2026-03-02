package io.jpm.core.m_entity.processor.handler;

import com.sun.source.util.Trees;
import io.jpm.api.MEntity;
import io.jpm.api.MField;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.AstHandler;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.core.m_entity.generator.domain.vo.DDLTableMetadata;
import io.jpm.core.m_entity.parse.domain.policy.TableMetadataFactory;
import io.jpm.core.m_entity.parse.domain.vo.MEntityInfo;
import io.jpm.core.m_entity.parse.infra.MetadataCacheV3;
import io.jpm.core.m_entity.parse.infra.ast.AstMFieldParserV2;
import io.jpm.core.m_entity.processor.handler.handlerContext.DDLHandlerContext;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateDDLMetadataHandler extends AstHandler<DDLHandlerContext> {


    private final Trees trees;
    private final MetadataCacheV3 metadataCache;
    public CreateDDLMetadataHandler(BuildTimeMetadataCache cache, GlobalRegistry globalRegistry, AstContext astContext) {
        super(cache, globalRegistry, astContext);
        this.trees = astContext.getTrees();

        this.metadataCache = new MetadataCacheV3(cache);
    }


    @Override
    public void handle(RoundEnvironment roundEnv)  {



        List<DDLTableMetadata> tables = new ArrayList<>();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MEntity.class);

        // --- Step 1. 모든 엔티티 사전 스캔 (Tree API 활용) ---
        for (Element element : elements) {
            if (element instanceof TypeElement) {

                scanEntity((TypeElement) element);
            }
        }

        // --- Step 2. 메타데이터 생성 (기존 로직 유지) ---


        for (String className : cache.getParsedVariablesCache().keySet()) {
            try {
                MEntityInfo currentEntityInfo = cache.getEntityInfoMap().get(className);
                // 여기서 꺼내는 데이터는 scanEntity에서 Tree API로 채운 데이터입니다.
                List<MField> fields = cache.getParsedVariablesCache().get(className);

                DDLTableMetadata table = TableMetadataFactory.create(fields, currentEntityInfo, cache.getEntityInfoMap());

                if (table != null) {
                    tables.add(table);
                }
            } catch (Exception e) {
                LogPrinter.exceptionInfo(e);
            }
        }

        handlerContext.setTables(tables);
        LogPrinter.info(tables.toString());

    }

    @Override
    public void setHandlerContext(DDLHandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }


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

            metadataCache.saveMetadataCache(classElement, allFieldsRawData);

            String simpleName = classElement.getSimpleName().toString(); // "OrderEntity"
            String entityName = extractEntityName(classElement);         // name 있으면 그 값, 없으면 simpleName

            cache.getRepoMetaRegistry().register(entityName, allFieldsRawData);

            // simpleName과 entityName이 다를 때만 alias 등록
            // ex) simpleName="OrderEntity", entityName="orders_alias"
            if (!simpleName.equals(entityName)) {
                cache.getRepoMetaRegistry().registerEntityAlias(simpleName, entityName);

            }

        } catch (Exception e) {
            LogPrinter.warn("AST Parsing failed for " + classElement.getSimpleName() + ": " + e.getMessage());
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
