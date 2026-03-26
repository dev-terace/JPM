package io.jpm.core.jpm_repository.valid.policy;

import io.jpm.common.exception.ErrorInfo;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.exception.ErrorCode;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.JoinNodeValidatorContext;
import io.jpm.core.jpm_repository.utils.ColumnResolver;
import io.jpm.core.jpm_repository.valid.domain.ValidateFkTypeMatchVO;

import java.util.Objects;


public class JoinNodeValidatorStep implements Step<JoinNodeValidatorContext> {

    private final EntityRelationRegistry entityRelationRegistry;
    private final RepoMetaRegistry repoMetaRegistry;
    private final ErrorTracker errorTracker;
    private final ColumnResolver columnResolver;


    public JoinNodeValidatorStep(BuildTimeMetadataCache cache, ErrorTracker errorTracker, ColumnResolver columnResolver) {
        this.entityRelationRegistry = cache.getEntityRelationRegistry();
        this.repoMetaRegistry = cache.getRepoMetaRegistry();
        this.errorTracker = errorTracker;
        this.columnResolver = columnResolver;
    }

    public void execute(JoinNodeValidatorContext ctx)
    {

        String leftCol = ctx.getLeftCol();
        String rightCol = ctx.getRightCol();

        String[] leftColInfo = columnResolver.resolve(leftCol);
        String[] rightColInfo = columnResolver.resolve(rightCol);
        String leftEntityName = leftColInfo[0];
        String rightEntityName = rightColInfo[0];
        String leftFieldName = leftColInfo[1];
        String rightFieldName = rightColInfo[1];

        EntityMeta leftColMeta = repoMetaRegistry.getEntityMeta(leftEntityName);
        EntityMeta rightColMeta = repoMetaRegistry.getEntityMeta(rightEntityName);


        String leftFieldType = leftColMeta.getFieldType(leftFieldName);
        String rightFieldType = rightColMeta.getFieldType(rightFieldName);



        validateFkTypeMatch(ValidateFkTypeMatchVO.builder()
                .leftEntityName(leftEntityName)
                .rightEntityName(rightEntityName)
                .leftFieldName(leftFieldName)
                .rightFieldName(rightFieldName)
                .leftFieldType(leftFieldType)
                .rightFieldType(rightFieldType)
                .build(), ctx);

    }

    private void validateFkTypeMatch(ValidateFkTypeMatchVO vo, JoinNodeValidatorContext ctx) {

        // leftCol이 FK인 경우

        String leftPkFieldName = entityRelationRegistry.getPkFieldName(vo.getLeftEntityName());
        String rightPkFieldName = entityRelationRegistry.getPkFieldName(vo.getRightEntityName());



        if (Objects.equals(leftPkFieldName, vo.getLeftFieldName()) && Objects.equals(rightPkFieldName, vo.getRightFieldName())) {
            errorTracker.addErrorInfo(ErrorInfo.builder()
                            .chainMethodName(ctx.getChainMethodName())
                            .methodName(ctx.getMethodName())
                            .className(ctx.getClassName())
                            .lineNumber(ctx.getLineNumber())
                            .err(ErrorCode.JOIN_DUP_PK)
                    .build());
        }


        boolean leftFkTrue = "FK".equals(vo.getLeftFieldType());
        boolean rightFkTrue = "FK".equals(vo.getRightFieldType());
        if(leftFkTrue || rightFkTrue) {
            if(leftFkTrue) {validateFkType(vo.getLeftFieldType(), vo.getLeftEntityName(), vo.getLeftFieldName(), vo.getRightFieldType(), ctx);}
            if(rightFkTrue) {validateFkType(vo.getRightFieldType(), vo.getRightEntityName(), vo.getRightFieldName(), vo.getLeftFieldType(), ctx);}
        }else{
            if(!vo.getLeftFieldType().equals(vo.getRightFieldType())) {errorTracker.addErrorInfo(ErrorCode.JOIN_TYPE_MISMATCH);}
        }
    }

    private void validateFkType(String sourceFieldType, String entityName, String fieldName, String targetType, JoinNodeValidatorContext ctx) {
        if ("FK".equals(sourceFieldType)) {
            String parentType = entityRelationRegistry.resolveFkType(entityName, fieldName);

            // 반대편 타입과 부모 타입이 맞지 않으면 에러 추가
            if (!targetType.equals(parentType)) {
                errorTracker.addErrorInfo(
                        ErrorInfo.builder()
                                .chainMethodName(ctx.getChainMethodName())
                                .methodName(ctx.getMethodName())
                                .lineNumber(ctx.getLineNumber())
                                .className(ctx.getClassName())
                                .err(ErrorCode.JOIN_TYPE_MISMATCH)
                                .build()
                        );
            }
        }
    }
}
