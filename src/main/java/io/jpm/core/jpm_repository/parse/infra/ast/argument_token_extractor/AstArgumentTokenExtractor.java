package io.jpm.core.jpm_repository.parse.infra.ast.argument_token_extractor;

import com.sun.source.tree.*;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.enums.ValueType;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.ArgumentTokenExtractorValueResolver;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.AstMethodTree;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils.AstTypeInferrerUtil;
import io.jpm.core.jpm_repository.valid.policy.ArgValidatorPolicy;
import io.jpm.common.utils.LogPrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DSL 메서드 호출(MethodInvocationTree)에서 인자 토큰 리스트를 추출합니다.
 * 기존 extractTokensTree() 의 단일 책임 분리 버전입니다.
 */
@Deprecated
public class AstArgumentTokenExtractor {

    private static final List<String> CONDITION_COMMANDS = Arrays.asList("where", "and", "or");

    private final ArgumentTokenExtractorValueResolver valueResolver;
    private final ArgValidatorPolicy validator;
    public AstArgumentTokenExtractor(ArgumentTokenExtractorValueResolver valueResolver) {
        this.valueResolver = valueResolver;

        this.validator = new ArgValidatorPolicy();
    }

    public List<String> extract(MethodInvocationTree call, MapParamRegistryImpl mapParamRegistryImpl, MethodMeta methodMeta) {
        try {
            List<String> result = new ArrayList<>();
            String command = AstMethodTree.getMethodName(call);
            boolean isCondition = CONDITION_COMMANDS.contains(command);

            List<? extends ExpressionTree> arguments = call.getArguments();

            for (int i = 0; i < arguments.size(); i++) {
                ExpressionTree arg = arguments.get(i);



                if (arg instanceof LambdaExpressionTree) {
                    LambdaExpressionTree lambda = (LambdaExpressionTree) arg;
                    if (lambda.getBody() instanceof MethodInvocationTree) {
                        MethodInvocationTree lambdaCall = (MethodInvocationTree) lambda.getBody();
                        for (ExpressionTree lambdaArg : lambdaCall.getArguments()) {
                            String resolved = valueResolver.resolve(lambdaArg, mapParamRegistryImpl, false, false);
                            result.add(resolved != null ? resolved : "");
                        }
                    }
                    continue;
                }




                boolean quoteString = isCondition && i == 2;

                String firstColumn = valueResolver.resolve(arguments.get(0), mapParamRegistryImpl, false, false);
                validator.saveFirstArgInfoIfMatched(firstColumn, command, i);

                ValueType valueType = validator.validateArgIfMatched(i, inferLiteralType(arg, command));

                String resolved = valueResolver.resolve(arg, mapParamRegistryImpl, quoteString, false);

                if(valueType != null) {
                    if(valueType.equals(ValueType.QUOTED)) {resolved = "'" + resolved + "'";}

                    LogPrinter.info("Resolved: " + resolved);
                }

                result.add(resolved != null ? resolved : "");
            }
            return result;
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    private String inferLiteralType(ExpressionTree conditionVal, String command) {

        if (!(conditionVal instanceof LiteralTree)) return null;


        return  AstTypeInferrerUtil.inferFromLiteralValue(((LiteralTree) conditionVal).getValue(), command);


    }
}