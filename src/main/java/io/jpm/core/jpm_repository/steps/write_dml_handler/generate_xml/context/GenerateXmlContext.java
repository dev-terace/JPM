package io.jpm.core.jpm_repository.steps.write_dml_handler.generate_xml.context;

import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.model.RepoMeta;
import io.jpm.core.jpm_repository.generator.MybatisXmlGenerator;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;

import java.util.List;

public class GenerateXmlContext  implements Context {

    private final JpmRepoContext                       jpmRepoContext;
    private final RepoMeta                             repoMeta;
    private final List<MybatisXmlGenerator.MethodData> methodDataList;

    public GenerateXmlContext(JpmRepoContext jpmRepoContext,
                              RepoMeta repoMeta,
                              List<MybatisXmlGenerator.MethodData> methodDataList) {
        this.jpmRepoContext = jpmRepoContext;
        this.repoMeta       = repoMeta;
        this.methodDataList = methodDataList;
    }

    public JpmRepoContext                       getJpmRepoContext() { return jpmRepoContext; }
    public RepoMeta                             getRepoMeta()       { return repoMeta; }
    public List<MybatisXmlGenerator.MethodData> getMethodDataList() { return methodDataList; }
}