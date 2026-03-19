package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils.AstTypeInferrerUtil;
import io.jpm.core.jpm_repository.valid.policy.ArgValidatorPolicyV2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentTokenExtractor {

    private static final List<String> CONDITION_COMMANDS = Arrays.asList("where", "and", "or");

    private final ArgumentTokenExtractorValueResolver valueResolver;
    private final ArgValidatorPolicyV2 validator;


    public ArgumentTokenExtractor(ArgumentTokenExtractorValueResolver valueResolver, BuildTimeMetadataCache cache , ErrorTracker errorTracker) {
        this.valueResolver = valueResolver;
        this.validator = new ArgValidatorPolicyV2(cache, errorTracker);
    }

    public List<String> extract(MethodInvocationTree call, MapParamRegistryImpl mapParamRegistryImpl) {
        try {
            String command = AstMethodTree.getMethodName(call);

            List<? extends ExpressionTree> arguments = call.getArguments();

            LogPrinter.info("AstArgumentTokenExtractorV2 mapParamRegistry = " + mapParamRegistryImpl.toString());
            boolean isCondition = CONDITION_COMMANDS.contains(command);

            // firstColumn을 루프 밖에서 한 번만 resolve
            String firstColumn = arguments.isEmpty()
                    ? null
                    : valueResolver.resolve(arguments.get(0), mapParamRegistryImpl, false, false);

            List<String> result = new ArrayList<>();

            for (int i = 0; i < arguments.size(); i++) {
                ExpressionTree arg = arguments.get(i);

                if (arg instanceof LambdaExpressionTree) {
                    result.addAll(extractFromLambda((LambdaExpressionTree) arg, mapParamRegistryImpl));
                    continue;
                }

                result.add(resolveNormalArg(arg, command, i, isCondition, firstColumn, mapParamRegistryImpl));
            }

            return result;
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    // lambda 처리 분리
    private List<String> extractFromLambda(LambdaExpressionTree lambda, MapParamRegistryImpl mapParamRegistryImpl) {
        List<String> tokens = new ArrayList<>();

        if (!(lambda.getBody() instanceof MethodInvocationTree)) return tokens;

        MethodInvocationTree lambdaCall = (MethodInvocationTree) lambda.getBody();
        tokens.add(AstMethodTree.getMethodName(lambdaCall));

        LogPrinter.info("extractFromLambda : " + AstMethodTree.getMethodName(lambdaCall));


        for (ExpressionTree lambdaArg : lambdaCall.getArguments()) {
            String resolved = valueResolver.resolve(lambdaArg, mapParamRegistryImpl, false, false);
            tokens.add(resolved != null ? resolved : "");
        }

        LogPrinter.info("AstArgumentTokenExtractorV2 lambda result = " + tokens);

        return tokens;
    }

    // 일반 인자 처리 분리
    private String resolveNormalArg(
            ExpressionTree arg, String command, int index,
            boolean isCondition, String firstColumn, MapParamRegistryImpl mapParamRegistryImpl
    ) {
        // 매직 넘버 제거: condition일 때 3번째(index=2) 인자는 값이므로 quote 대상
        boolean quoteString = isCondition && index == 2;
        String resolved = valueResolver.resolve(arg, mapParamRegistryImpl, quoteString, false);
        return resolved != null ? resolved : "";
    }

    private String inferLiteralType(ExpressionTree conditionVal, String command) {

        if (!(conditionVal instanceof LiteralTree)) return null;


        return  AstTypeInferrerUtil.inferFromLiteralValue(((LiteralTree) conditionVal).getValue(), command);


    }
}
