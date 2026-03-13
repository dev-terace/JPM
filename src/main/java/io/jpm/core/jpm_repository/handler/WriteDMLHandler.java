package io.jpm.core.jpm_repository.handler;

import io.jpm.config.ast.*;
import io.jpm.core.jpm_repository.domain.model.RepoMeta;
import io.jpm.core.jpm_repository.generator.MybatisXmlGenerator;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;

import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.main.BuildMethodDataMainStep;
import io.jpm.core.jpm_repository.steps.write_dml_handler.generate_xml.main.GenerateXmlMainStep;
import io.jpm.core.jpm_repository.steps.write_dml_handler.ValidateErrorMainStep;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context.BuildMethodDataContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.generate_xml.context.GenerateXmlContext;


import java.util.List;

public class WriteDMLHandler implements AbstractHandler<JpmRepoContext> {

    private final BuildMethodDataMainStep buildMethodDataMainStep = new BuildMethodDataMainStep();
    private final ValidateErrorMainStep validateErrorMainStep = new ValidateErrorMainStep();
    private final GenerateXmlMainStep generateXmlMainStep = new GenerateXmlMainStep();



    @Override
    public void handle(JpmRepoContext context) throws Exception {
        for (RepoMeta repoMeta : context.getRepoMetas()) {
            buildMethodDataMainStep.execute(new BuildMethodDataContext(context, repoMeta));

            validateErrorMainStep.execute(context);

            List<MybatisXmlGenerator.MethodData> methodDataList = buildMethodDataMainStep.getResult();

            if (!methodDataList.isEmpty()) {
                generateXmlMainStep.execute(new GenerateXmlContext(context, repoMeta, methodDataList));
            }
        }
    }


}