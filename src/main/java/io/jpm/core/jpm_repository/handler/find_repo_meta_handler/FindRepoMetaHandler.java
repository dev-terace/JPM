package io.jpm.core.jpm_repository.handler.find_repo_meta_handler;

import io.jpm.config.ast.*;
import io.jpm.core.jpm_repository.handler.handlerContext.JpmRepoContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.main.FindRepoMetaMainStep;


//내일 할거
//현재 segment가 scopeName 기준으로 cache에서 repoClass, scopeName 식으로 segment 타겟 클래스를 찾음
//segmentPath cache를 삭제하고 segmentInliner에서 arg targetClass와 메서드를 읽어야함 


//app config 로 전부 globalRegistry로 넘김

public class FindRepoMetaHandler implements AbstractHandler<JpmRepoContext> {





    @Override
    public void handle(JpmRepoContext context) throws Exception {

        new FindRepoMetaMainStep(context.getGlobalRegistry(), context.getContext()).execute(context);

    }





}
