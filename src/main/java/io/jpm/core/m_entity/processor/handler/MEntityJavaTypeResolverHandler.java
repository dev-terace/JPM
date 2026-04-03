package io.jpm.core.m_entity.processor.handler;

import io.jpm.api.MEntity;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.*;
import io.jpm.core.m_entity.domain.MFieldJavaTypeMeta;
import io.jpm.core.m_entity.processor.handler.handlerContext.DDLHandlerContext;
import io.jpm.core.m_entity.steps.MEntityJavaTypeResolverHandler.context.CollectMEntityContext;
import io.jpm.core.m_entity.steps.MEntityJavaTypeResolverHandler.context.GeneratedJavaTypeContext;
import io.jpm.core.m_entity.steps.MEntityJavaTypeResolverHandler.main.CollectMEntityMainStep;
import io.jpm.core.m_entity.steps.MEntityJavaTypeResolverHandler.main.GeneratedJavaTypeMainStep;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MEntityJavaTypeResolverHandler extends AstHandler<DDLHandlerContext>
{
    private final AstContext astContext;

    private final Step<CollectMEntityContext> collectMEntityMainStep;
    private final Step<GeneratedJavaTypeContext> generatedJavaTypeMainStepStep;

    public MEntityJavaTypeResolverHandler(BuildTimeMetadataCache cache, GlobalRegistry globalRegistry, AstContext astContext) {
        super(cache, globalRegistry, astContext);
        this.astContext = astContext;
        this.collectMEntityMainStep = new CollectMEntityMainStep(cache.getEntityRelationRegistry());
        this.generatedJavaTypeMainStepStep = new GeneratedJavaTypeMainStep();
    }

    @Override
    public void handle(RoundEnvironment roundEnv) throws Exception {


        CollectMEntityContext ctx = new CollectMEntityContext(roundEnv, astContext.getFiler(), astContext.getTrees());


        collectMEntityMainStep.execute(ctx);

        LogPrinter.info("[MEntityJavaTypeResolverHandler] Collect MEntity main step " + ctx.getMetas());
        generatedJavaTypeMainStepStep.execute(new GeneratedJavaTypeContext(ctx.getMetas()));



/*        List<MFieldJavaTypeMeta> metas = new ArrayList<>();

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MEntity.class);
        for (Element element : elements) {
            if (element instanceof TypeElement) {

                TypeElement typeElement = (TypeElement) element;

                MFieldJavaTypeMeta meta = new MFieldJavaTypeMeta();

                String fqcn = typeElement.getQualifiedName().toString();

                meta.setGeneratePath(fqcn);

                for (Element enclosed : typeElement.getEnclosedElements()) {
                    if (enclosed.getKind() != ElementKind.FIELD) continue;
                    VariableElement fieldEl = (VariableElement) enclosed;
                    if (!(fieldEl.asType() instanceof DeclaredType)) continue;
                    DeclaredType declaredType = (DeclaredType) fieldEl.asType();
                    String fieldTypeName = declaredType.asElement()
                            .getSimpleName().toString();
                    // MField 인지 확인
                    if (!fieldTypeName.equals("MField")) continue;
                    String fieldName = fieldEl.getSimpleName().toString();
                    List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
                    String mFieldType = ((DeclaredType) typeArgs.get(0)).asElement()
                            .getSimpleName().toString();
                    meta.addTypeInfo(fieldName, mFieldType);
                    System.out.println("[MEntityJavaTypeResolver] fieldName : " + fieldName);
                    System.out.println("[MEntityJavaTypeResolver] mFieldType: " + mFieldType);
                }
                metas.add(meta);
            }
        }*/
    }

    @Override
    public void setHandlerContext(DDLHandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }
}
