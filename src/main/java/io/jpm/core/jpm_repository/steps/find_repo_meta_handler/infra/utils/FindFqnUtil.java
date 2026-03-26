package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.List;

public class FindFqnUtil {


    public static String findClassNameFqnOrInterface(TypeElement repoElement, List<String> passedArgs, AstContext astContext)
    {
        String arg = passedArgs.get(0);

        // 클래스 리터럴인 경우
        if (arg.contains(".class")) {
            String simpleName = arg.replace(".class", "");
            String fqn = FindFqnUtil.findFqnFromImports(repoElement, simpleName, astContext);
            if (!fqn.isEmpty()) return fqn;

            String pkg = astContext.getElements()
                    .getPackageOf(repoElement)
                    .getQualifiedName()
                    .toString();
            return pkg + "." + simpleName;
        }

        // 변수인 경우 — 필드 초기값에서 클래스명 추출
        String resolved = resolveFromFieldInitializer(repoElement, arg, astContext);
        if (resolved != null) return resolved;

        LogPrinter.error("[FindFqn] 변수 '" + arg + "' 의 값을 resolve할 수 없습니다.");
        return "";
    }


    private static String resolveFromFieldInitializer(TypeElement repoElement, String variableName, AstContext astContext) {

        for (Element enclosed : repoElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.FIELD) continue;
            if (!enclosed.getSimpleName().toString().equals(variableName)) continue;

            Tree node = astContext.getTrees().getTree(enclosed);
            if (!(node instanceof VariableTree)) continue;
            VariableTree varTree = (VariableTree) node;

            ExpressionTree initializer = varTree.getInitializer();
            if (initializer == null) continue;

            String initStr = initializer.toString();
            if (!initStr.contains(".class")) continue;

            String simpleName = initStr.replace(".class", "");
            String fqn = FindFqnUtil.findFqnFromImports(repoElement, simpleName, astContext);
            if (!fqn.isEmpty()) return fqn;

            String pkg = astContext.getElements()
                    .getPackageOf(repoElement)
                    .getQualifiedName()
                    .toString();
            return pkg + "." + simpleName;
        }

        LogPrinter.error("[SegmentInliner] 필드 '" + variableName + "' 를 찾을 수 없거나 .class 리터럴이 아닙니다.");
        return null;
    }

    public static String findFqnFromImports(TypeElement repoElement, String simpleName, AstContext astContext)
    {
        TreePath path          = astContext.getTrees().getPath(repoElement);
        CompilationUnitTree cu = path.getCompilationUnit();
        for (ImportTree imp : cu.getImports()) {
            String importStr = imp.getQualifiedIdentifier().toString();
            if (importStr.endsWith("." + simpleName)) return importStr;
        }
        return "";
    }
}
