package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.core;

import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.ParamTypeCollectorContext;

public class ParamTypeCollectorStep implements Step<ParamTypeCollectorContext> {



    @Override
    public void execute(ParamTypeCollectorContext context) throws Exception {
        //1. ctx param, RepoMetaRegistry, args, command
        //2. args를 읽음 contain으로 List<Args> #이 들어있는지 확인
        //command를 case문으로 having where on and 등등 확인
        //args에 :: 인자값을 파싱 class | 필드이름 -> repometaRegistry entityMeta -> entityMeta.getFieldType(필드이름)
        // 타입마다 파싱 LocalDate ... 등등등
        //캐쉬 에다가 넣기



    }
}
