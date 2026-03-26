package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.MethodParseContext;

import javax.lang.model.element.TypeElement;

public class TreePositionUtil {

    public static int getLineNumber(Trees trees, MethodInvocationTree call, TypeElement element)
    {
        TreePath classPath = trees.getPath(element);
        CompilationUnitTree cu = classPath.getCompilationUnit();


        SourcePositions sp = trees.getSourcePositions();
        long pos = sp.getStartPosition(cu, call);

        return Math.toIntExact(cu.getLineMap().getLineNumber(pos));
    }
}
