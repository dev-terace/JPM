package io.jpm.core.jpm_repository.steps.write_dml_handler;

import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.generator.MybatisXmlGenerator;
import io.jpm.core.jpm_repository.steps.write_dml_handler.context.GenerateXmlContext;

public class GenerateXmlStep implements Step<GenerateXmlContext> {

    @Override
    public void execute(GenerateXmlContext ctx) throws Exception {
        MybatisXmlGenerator xmlGenerator = new MybatisXmlGenerator(
                ctx.getJpmRepoContext().getCache().getRepoMetaRegistry()
        );
        String resultXml = xmlGenerator.generateXml(
                ctx.getRepoMeta().getNamespace(),
                ctx.getMethodDataList()
        );
        LogPrinter.info("\n[완성된 MyBatis XML]\n" + resultXml);
    }
}