package io.jpm.core.jpm_repository.parse.infra.ast.argument_token_extractor;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.core.jpm_repository.parse.domain.vo.MethodMeta;
import io.jpm.core.jpm_repository.parse.domain.vo.ValueType;
import io.jpm.core.jpm_repository.parse.infra.MapParamRegistry;
import io.jpm.core.jpm_repository.parse.infra.ast.AstExpressionTreeValueResolver;
import io.jpm.core.jpm_repository.parse.infra.ast.AstMethodTreeUtil;
import io.jpm.core.jpm_repository.valid.policy.ArgValidatorPolicy;
import io.jpm.core.jpm_repository.valid.policy.ArgValidatorPolicyV2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AstArgumentTokenExtractorV2 {

    private static final List<String> CONDITION_COMMANDS = Arrays.asList("where", "and", "or");

    private final AstExpressionTreeValueResolver valueResolver;
    private final ArgValidatorPolicyV2 validator;


    public AstArgumentTokenExtractorV2(AstExpressionTreeValueResolver valueResolver, BuildTimeMetadataCache cache) {
        this.valueResolver = valueResolver;
        this.validator = new ArgValidatorPolicyV2(cache);



    }

    public List<String> extract(MethodInvocationTree call, MapParamRegistry mapParamRegistry) {
        try {
            List<String> result = new ArrayList<>();
            String command = AstMethodTreeUtil.getMethodName(call);
            boolean isCondition = CONDITION_COMMANDS.contains(command);

            List<? extends ExpressionTree> arguments = call.getArguments();

            for (int i = 0; i < arguments.size(); i++) {
                ExpressionTree arg = arguments.get(i);
                boolean quoteString = isCondition && i == 2;

                String firstColumn = valueResolver.resolve(arguments.get(0), mapParamRegistry, false, false);
                validator.saveFirstArgInfoIfMatched(firstColumn, command, i);

                ValueType valueType = validator.validateArgIfMatched(i, inferLiteralType(arg, command));

                String resolved = valueResolver.resolve(arg, mapParamRegistry, quoteString, false);

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


        return  AstTypeInferrer.inferFromLiteralValue(((LiteralTree) conditionVal).getValue(), command);


    }
}
