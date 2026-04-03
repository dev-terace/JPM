package io.jpm.core.m_entity.steps.MEntityJavaTypeResolverHandler.main;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import io.jpm.api.MEntity;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.m_entity.domain.MFieldJavaTypeMeta;
import io.jpm.core.m_entity.steps.MEntityJavaTypeResolverHandler.context.CollectMEntityContext;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectMEntityMainStep implements Step<CollectMEntityContext> {

    private final EntityRelationRegistry entityRelationRegistry;

    public CollectMEntityMainStep(EntityRelationRegistry entityRelationRegistry) {
        this.entityRelationRegistry = entityRelationRegistry;
    }


    @Override
    public void execute(CollectMEntityContext ctx) {

        Set<? extends Element> elements = ctx.getRoundEnv()
                .getElementsAnnotatedWith(MEntity.class);

        for (Element element : elements) {
            if (!(element instanceof TypeElement)) continue;

            TypeElement typeElement = (TypeElement) element;
            MFieldJavaTypeMeta meta = new MFieldJavaTypeMeta(ctx.getFiler());
            meta.setGeneratePath(typeElement.getQualifiedName().toString());

            for (Element enclosed : typeElement.getEnclosedElements()) {
                if (enclosed.getKind() != ElementKind.FIELD) continue;

                VariableElement fieldEl = (VariableElement) enclosed;
                if (!(fieldEl.asType() instanceof DeclaredType)) continue;

                DeclaredType declaredType = (DeclaredType) fieldEl.asType();

                if (!declaredType.asElement().getSimpleName().contentEquals("MField")) continue;

                String fieldName = fieldEl.getSimpleName().toString();

                List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
                if (typeArgs.isEmpty()) continue;

                String mFieldType = ((DeclaredType) typeArgs.get(0))
                        .asElement().getSimpleName().toString();


                if(mFieldType.equals("FK"))
                {

                    TreePath path = ctx.getTrees().getPath(fieldEl);
                    VariableTree variableTree = (VariableTree) path.getLeaf();


                    ExpressionTree initializer = variableTree.getInitializer();

                    String parentEntityName = extractParentClass(initializer);


                    mFieldType = entityRelationRegistry.getPkFieldType(parentEntityName);
                }

                meta.addTypeInfo(fieldName, mFieldType);

                System.out.println("[CollectMEntityStep] fieldName : " + fieldName);
                System.out.println("[CollectMEntityStep] mFieldType: " + mFieldType);
            }

            ctx.addMeta(meta);

        }

        LogPrinter.info("[CollectMEntityStep] meta list complete " + ctx.getMetas());
    }


    private String extractParentClass(ExpressionTree expr) {

        while (expr instanceof MethodInvocationTree) {

            MethodInvocationTree method = (MethodInvocationTree) expr;

            String methodName = method.getMethodSelect().toString();

            if (methodName.endsWith(".parent")) {

                // parent(OrderEntity.class)
                ExpressionTree arg = method.getArguments().get(0);

                return arg.toString().replace(".class", "");
            }

            // 다음 체인으로 이동
            expr = ((MemberSelectTree) method.getMethodSelect()).getExpression();
        }

        return null;
    }

}
