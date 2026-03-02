package io.jpm.config.ast;

import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import io.jpm.common.exception.config.JpmToolbox;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public final class AstContext {

    private final Trees trees;
    private final TreeMaker treeMaker;
    private final Names names;
    private final Messager messager;
    private final Elements elements;
    private final Types types;
    private final Filer filer; // ✅ 추가
    private final JpmToolbox jpmToolbox;

    public AstContext(ProcessingEnvironment processingEnv) {

        this.trees = JavacTrees.instance(processingEnv);
        this.jpmToolbox = new JpmToolbox(processingEnv);
        JavacProcessingEnvironment javacEnv =
                (JavacProcessingEnvironment) processingEnv;

        Context context = javacEnv.getContext();

        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);

        this.messager = processingEnv.getMessager();
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler(); // ✅ 추가
    }

    // ===== Getter =====

    public JpmToolbox getJpmToolbox() {
        return jpmToolbox;
    }

    public Trees getTrees() {
        return trees;
    }

    public TreeMaker getTreeMaker() {
        return treeMaker;
    }

    public Names getNames() {
        return names;
    }

    public Messager getMessager() {
        return messager;
    }

    public Elements getElements() {
        return elements;
    }

    public Types getTypes() {
        return types;
    }

    public Filer getFiler() {   // ✅ 추가
        return filer;
    }

    public JCTree.JCIdent createIdent(String name) {
        return treeMaker.Ident(names.fromString(name));
    }
}