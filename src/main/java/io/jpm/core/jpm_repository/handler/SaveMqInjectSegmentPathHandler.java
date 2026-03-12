package io.jpm.core.jpm_repository.handler;

import io.jpm.api.MqInject;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.AstHandler;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;



@Deprecated
public class SaveMqInjectSegmentPathHandler extends AstHandler<JpmRepoContext> {

    private final AstContext astContext;
    public SaveMqInjectSegmentPathHandler(BuildTimeMetadataCache cache, GlobalRegistry globalRegistry, AstContext astContext) {
        super(cache, globalRegistry, astContext);
        this.astContext = astContext;
    }


    @Override
    public void handle(RoundEnvironment roundEnv) throws Exception {
        for (Element element : roundEnv.getElementsAnnotatedWith(MqInject.class)) {
            if (element.getKind() != ElementKind.FIELD) continue;

            VariableElement field = (VariableElement) element;
            TypeMirror fieldType = field.asType();
            Element typeElement = astContext.getTypes().asElement(fieldType);

            if (!(typeElement instanceof TypeElement)) continue;

            String segmentFqcn = ((TypeElement) typeElement).getQualifiedName().toString();
            String repoName    = field.getEnclosingElement().getSimpleName().toString();
            String varName     = field.getSimpleName().toString(); // "orderSegment"

            cache.getRepoMetaRegistry().registerSegmentPath(repoName, varName, segmentFqcn);

        }
    }

    @Override
    public void setHandlerContext(JpmRepoContext handlerContext) {

    }
}
