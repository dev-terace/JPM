package io.jpm.core.m_entity.steps.MEntityJavaTypeResolverHandler.context;

import io.jpm.config.ast.Context;
import io.jpm.core.m_entity.domain.MFieldJavaTypeMeta;

import java.util.List;

public class GeneratedJavaTypeContext implements Context {

    private final List<MFieldJavaTypeMeta> metas;

    public GeneratedJavaTypeContext(List<MFieldJavaTypeMeta> metas) {
        this.metas = metas;
    }

    public List<MFieldJavaTypeMeta> getMetas() {
        return metas;
    }
}
