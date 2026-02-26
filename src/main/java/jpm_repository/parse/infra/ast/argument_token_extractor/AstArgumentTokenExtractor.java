package jpm_repository.parse.infra.ast.argument_token_extractor;

import com.sun.source.tree.*;
import exception.ErrorCollector;
import jpm_repository.parse.infra.MapParamRegistryImpl;
import jpm_repository.parse.domain.vo.ValueType;
import jpm_repository.parse.domain.vo.MethodMeta;
import jpm_repository.parse.infra.ast.AstExpressionTreeValueResolver;
import jpm_repository.parse.infra.ast.AstMethodTreeUtil;
import jpm_repository.valid.policy.ArgValidatorPolicy;
import utils.LogPrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DSL 메서드 호출(MethodInvocationTree)에서 인자 토큰 리스트를 추출합니다.
 * 기존 extractTokensTree() 의 단일 책임 분리 버전입니다.
 */
public class AstArgumentTokenExtractor {

    private static final List<String> CONDITION_COMMANDS = Arrays.asList("where", "and", "or");

    private final AstExpressionTreeValueResolver valueResolver;

    public AstArgumentTokenExtractor(AstExpressionTreeValueResolver valueResolver) {
        this.valueResolver = valueResolver;
    }

    public List<String> extract(MethodInvocationTree call, MapParamRegistryImpl mapParamRegistryImpl, MethodMeta methodMeta) {
        try {
            List<String> result = new ArrayList<>();
            String command = AstMethodTreeUtil.getMethodName(call);
            boolean isCondition = CONDITION_COMMANDS.contains(command);

            List<? extends ExpressionTree> arguments = call.getArguments();
            ArgValidatorPolicy validator = new ArgValidatorPolicy();
            for (int i = 0; i < arguments.size(); i++) {
                ExpressionTree arg = arguments.get(i);
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

    private String inferLiteralType(ExpressionTree arg, String command) {
        if (!(arg instanceof LiteralTree)) return null;
        return AstTypeInferrer.inferFromLiteralValue(((LiteralTree) arg).getValue(), command);
    }
}