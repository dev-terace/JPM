package io.jpm.core.jpm_repository.processor.handler.handlerContext;

import io.jpm.core.jpm_repository.generator.infra.mapper.MybatisXmlGenerator;
import io.jpm.core.jpm_repository.parse.domain.vo.MethodMeta;
import io.jpm.core.jpm_repository.parse.domain.vo.RepoMeta;

import java.util.Collections;
import java.util.List;

public class AstJpmRepoContext {

    private List<RepoMeta> repoMetas;


    private List<MybatisXmlGenerator.MethodData> methodDataList;

    public List<MybatisXmlGenerator.MethodData> getMethodDataList() {
        return methodDataList;
    }

    public void setMethodDataList(List<MybatisXmlGenerator.MethodData> methodDataList) {
        this.methodDataList = methodDataList;
    }

    public List<RepoMeta> getRepoMetas() {
        return repoMetas;
    }

    public void setRepoMetas(List<RepoMeta> repoMetas) {
        this.repoMetas = repoMetas;
    }
}