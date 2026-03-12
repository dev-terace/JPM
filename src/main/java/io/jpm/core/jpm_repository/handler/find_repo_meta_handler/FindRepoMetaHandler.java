package io.jpm.core.jpm_repository.handler.find_repo_meta_handler;

import com.sun.source.tree.*;
import com.sun.source.util.Trees;
import io.jpm.api.JpmRepository;
import io.jpm.config.ast.*;
import io.jpm.core.jpm_repository.domain.model.RepoMeta;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.FindRepoMetaStep;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



//내일 할거
//현재 segment가 scopeName 기준으로 cache에서 repoClass, scopeName 식으로 segment 타겟 클래스를 찾음
//segmentPath cache를 삭제하고 segmentInliner에서 arg targetClass와 메서드를 읽어야함 


//app config 로 전부 globalRegistry로 넘김

public class FindRepoMetaHandler implements AbstractHandler<JpmRepoContext> {





    @Override
    public void handle(JpmRepoContext context) throws Exception {

        new FindRepoMetaStep(context.getGlobalRegistry(), context.getContext()).execute(context);

    }





}
