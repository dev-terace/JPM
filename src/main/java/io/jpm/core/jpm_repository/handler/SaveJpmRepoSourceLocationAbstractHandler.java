package io.jpm.core.jpm_repository.handler;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.jpm.api.JpmQuerySegment;
import io.jpm.api.JpmRepository;
import io.jpm.common.exception.config.JpmToolbox;
import io.jpm.config.ast.*;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;
import io.jpm.core.jpm_repository.steps.save_jpm_repo_source_location_handler.SaveSourceLocationMainStep;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;


public class SaveJpmRepoSourceLocationAbstractHandler implements AbstractHandler<JpmRepoContext> {

    private final Step<JpmRepoContext> saveSourceLocationStep = new SaveSourceLocationMainStep();






    @Override
    public void handle(JpmRepoContext context) throws Exception {
        saveSourceLocationStep.execute(context);

    }
}
