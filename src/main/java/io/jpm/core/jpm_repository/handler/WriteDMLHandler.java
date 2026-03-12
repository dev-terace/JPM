package io.jpm.core.jpm_repository.handler;

import io.jpm.config.ast.*;
import io.jpm.core.jpm_repository.domain.model.RepoMeta;
import io.jpm.core.jpm_repository.generator.MybatisXmlGenerator;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;

import io.jpm.core.jpm_repository.steps.write_dml_handler.BuildMethodDataStep;
import io.jpm.core.jpm_repository.steps.write_dml_handler.GenerateXmlStep;
import io.jpm.core.jpm_repository.steps.write_dml_handler.ValidateErrorStep;
import io.jpm.core.jpm_repository.steps.write_dml_handler.context.BuildMethodDataContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.context.GenerateXmlContext;


import java.util.List;

public class WriteDMLHandler implements AbstractHandler<JpmRepoContext> {

    private final BuildMethodDataStep buildMethodDataStep = new BuildMethodDataStep();
    private final ValidateErrorStep validateErrorStep   = new ValidateErrorStep();
    private final GenerateXmlStep generateXmlStep     = new GenerateXmlStep();



    @Override
    public void handle(JpmRepoContext context) throws Exception {
        for (RepoMeta repoMeta : context.getRepoMetas()) {
            buildMethodDataStep.execute(new BuildMethodDataContext(context, repoMeta));

            validateErrorStep.execute(context);

            List<MybatisXmlGenerator.MethodData> methodDataList = buildMethodDataStep.getResult();

            if (!methodDataList.isEmpty()) {
                generateXmlStep.execute(new GenerateXmlContext(context, repoMeta, methodDataList));
            }
        }
    }


}