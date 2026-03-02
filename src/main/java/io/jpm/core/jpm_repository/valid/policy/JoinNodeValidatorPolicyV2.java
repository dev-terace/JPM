package io.jpm.core.jpm_repository.valid.policy;

import io.jpm.common.exception.ErrorTracker;
import io.jpm.config.AppConfig;
import io.jpm.common.exception.ErrorCode;
import io.jpm.common.exception.ErrorCollector;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.core.jpm_repository.parse.domain.cache.EntityRelationRegistry;
import io.jpm.core.jpm_repository.parse.domain.vo.EntityMeta;
import io.jpm.core.jpm_repository.generator.infra.utils.ColumnResolver;
import io.jpm.core.jpm_repository.valid.domain.ValidateFkTypeMatchVO;
import io.jpm.common.utils.LogPrinter;

import java.util.Objects;

public class JoinNodeValidatorPolicyV2 {

    private final EntityRelationRegistry entityRelationRegistry;
    private final RepoMetaRegistry repoMetaRegistry;
    private final ErrorTracker errorTracker;
    private final ColumnResolver columnResolver;


    public JoinNodeValidatorPolicyV2(BuildTimeMetadataCache cache, ColumnResolver columnResolver) {
        this.entityRelationRegistry = cache.getEntityRelationRegistry();
        this.repoMetaRegistry = cache.getRepoMetaRegistry();
        this.errorTracker = cache.getErrorTracker();
        this.columnResolver = columnResolver;
    }

    public void validateJoinType(String command, String leftCol, String rightCol)
    {

        errorTracker.setChainMethodName(command);

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
        LogPrinter.info("leftFieldName: " + leftFieldName);
        LogPrinter.info("rightFieldName: " + rightFieldName);


        validateFkTypeMatch(ValidateFkTypeMatchVO.builder()
                .leftEntityName(leftEntityName)
                .rightEntityName(rightEntityName)
                .leftFieldName(leftFieldName)
                .rightFieldName(rightFieldName)
                .leftFieldType(leftFieldType)
                .rightFieldType(rightFieldType)
                .build());

    }

    private void validateFkTypeMatch(ValidateFkTypeMatchVO vo) {

        // leftCol이 FK인 경우

        String leftPkFieldName = entityRelationRegistry.getPkFieldName(vo.getLeftEntityName());
        String rightPkFieldName = entityRelationRegistry.getPkFieldName(vo.getRightEntityName());



        if (Objects.equals(leftPkFieldName, vo.getLeftFieldName()) && Objects.equals(rightPkFieldName, vo.getRightFieldName())) {
            errorTracker.addErrorInfo(ErrorCode.JOIN_DUP_PK);
        }


        boolean leftFkTrue = "FK".equals(vo.getLeftFieldType());
        boolean rightFkTrue = "FK".equals(vo.getRightFieldType());
        if(leftFkTrue || rightFkTrue) {
            if(leftFkTrue) {validateFkType(vo.getLeftFieldType(), vo.getLeftEntityName(), vo.getLeftFieldName(), vo.getRightFieldType());}
            if(rightFkTrue) {validateFkType(vo.getRightFieldType(), vo.getRightEntityName(), vo.getRightFieldName(), vo.getLeftFieldType());}
        }else{
            if(!vo.getLeftFieldType().equals(vo.getRightFieldType())) {errorTracker.addErrorInfo(ErrorCode.JOIN_TYPE_MISMATCH);}
        }
    }

    private void validateFkType(String sourceFieldType, String entityName, String fieldName, String targetType) {
        if ("FK".equals(sourceFieldType)) {
            String parentType = entityRelationRegistry.resolveFkType(entityName, fieldName);

            // 반대편 타입과 부모 타입이 맞지 않으면 에러 추가
            if (!targetType.equals(parentType)) {
                errorTracker.addErrorInfo(ErrorCode.JOIN_TYPE_MISMATCH);
            }
        }
    }
}
