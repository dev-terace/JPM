package io.jpm.core.m_entity.steps.MEntityJavaTypeResolverHandler.context;


import com.sun.source.util.Trees;
import io.jpm.config.ast.Context;
import io.jpm.core.m_entity.domain.MFieldJavaTypeMeta;

import javax.annotation.processing.Filer;

import javax.annotation.processing.RoundEnvironment;
import java.util.ArrayList;
import java.util.List;

public class CollectMEntityContext implements Context {

    private final RoundEnvironment roundEnv;
    private final List<MFieldJavaTypeMeta> metas = new ArrayList<>();
    private final Trees trees;
    private final Filer filer;
    public CollectMEntityContext(RoundEnvironment roundEnv, Filer filer, Trees trees) {
        this.roundEnv = roundEnv;
        this.filer = filer;
        this.trees = trees;
    }


    public Trees getTrees() {
        return trees;
    }

    public Filer getFiler() {
        return filer;
    }

    public RoundEnvironment getRoundEnv()               { return roundEnv; }
    public List<MFieldJavaTypeMeta> getMetas()          { return metas; }
    public void addMeta(MFieldJavaTypeMeta meta)        { metas.add(meta); }

}
