package jpm_repository.valid.policy;

import config.AppConfig;
import exception.ErrorCode;
import exception.ErrorCollector;
import jpm_repository.parse.domain.cache.RepoMetaRegistry;
import jpm_repository.parse.domain.cache.EntityRelationRegistry;
import jpm_repository.parse.domain.vo.EntityMeta;
import jpm_repository.generator.infra.utils.ColumnResolver;
import jpm_repository.valid.domain.ValidateFkTypeMatchVO;
import utils.LogPrinter;

import java.util.Objects;

public class JoinNodeValidatorPolicy {

    private static final EntityRelationRegistry entityRelationRegistry = AppConfig.getEntityRelationRegistry();
    private static final RepoMetaRegistry REPO_META_REGISTRY = AppConfig.getEntityMetaRegistry();



    public static void validateJoinType(String command, String leftCol, String rightCol)
    {

        ErrorCollector.setChainMethodName(command);

        String[] leftColInfo = ColumnResolver.resolve(leftCol);
        String[] rightColInfo = ColumnResolver.resolve(rightCol);
        String leftEntityName = leftColInfo[0];
        String rightEntityName = rightColInfo[0];
        String leftFieldName = leftColInfo[1];
        String rightFieldName = rightColInfo[1];

        EntityMeta leftColMeta = REPO_META_REGISTRY.getEntityMeta(leftEntityName);
        EntityMeta rightColMeta = REPO_META_REGISTRY.getEntityMeta(rightEntityName);


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

    private static void validateFkTypeMatch(ValidateFkTypeMatchVO vo) {

        // leftCol이 FK인 경우


        String leftPkFieldName = entityRelationRegistry.getPkFieldName(vo.getLeftEntityName());
        String rightPkFieldName = entityRelationRegistry.getPkFieldName(vo.getRightEntityName());



        if (Objects.equals(leftPkFieldName, vo.getLeftFieldName()) && Objects.equals(rightPkFieldName, vo.getRightFieldName())) {

            ErrorCollector.addErrorInfo(ErrorCode.JOIN_DUP_PK);
        }


        boolean leftFkTrue = "FK".equals(vo.getLeftFieldType());
        boolean rightFkTrue = "FK".equals(vo.getRightFieldType());
        if(leftFkTrue || rightFkTrue) {
            if(leftFkTrue) {validateFkType(vo.getLeftFieldType(), vo.getLeftEntityName(), vo.getLeftFieldName(), vo.getRightFieldType());}
            if(rightFkTrue) {validateFkType(vo.getRightFieldType(), vo.getRightEntityName(), vo.getRightFieldName(), vo.getLeftFieldType());}
        }else{
            if(!vo.getLeftFieldType().equals(vo.getRightFieldType())) {ErrorCollector.addErrorInfo(ErrorCode.JOIN_TYPE_MISMATCH);}
        }

/*        if ("FK".equals(vo.getLeftFieldType())) {
            String leftParentFieldType = entityRelationRegistry.resolveFkType(vo.getLeftEntityName(), vo.getLeftFieldName());

            if (!vo.getRightFieldType().equals(leftParentFieldType))
            {

                ErrorCollector.addErrorInfo(ErrorCode.JOIN_TYPE_MISMATCH);
            }

        }

        // rightCol이 FK인 경우
        if ("FK".equals(vo.getRightFieldType())) {
            String rightParentFieldType = entityRelationRegistry.resolveFkType(vo.getRightEntityName(), vo.getRightFieldName());
            if (!vo.getLeftFieldType().equals(rightParentFieldType)) {

                ErrorCollector.addErrorInfo(ErrorCode.JOIN_TYPE_MISMATCH);
            }
        }*/
    }

    private static void validateFkType(String sourceFieldType, String entityName, String fieldName, String targetType) {
        if ("FK".equals(sourceFieldType)) {
            String parentType = entityRelationRegistry.resolveFkType(entityName, fieldName);

            // 반대편 타입과 부모 타입이 맞지 않으면 에러 추가
            if (!targetType.equals(parentType)) {
                ErrorCollector.addErrorInfo(ErrorCode.JOIN_TYPE_MISMATCH);
            }
        }
    }
}
