package mq_mapper.infra.ast;

import com.sun.source.tree.*;
import mq_mapper.domain.vo.MethodMeta;
import utils.LogPrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DSL 메서드 호출(MethodInvocationTree)에서 인자 토큰 리스트를 추출합니다.
 * 기존 extractTokensTree() 의 단일 책임 분리 버전입니다.
 */
public class ArgumentTokenExtractor {

    private static final List<String> CONDITION_COMMANDS = Arrays.asList("where", "and", "or");

    private final ExpressionTreeValueResolver valueResolver;

    public ArgumentTokenExtractor(ExpressionTreeValueResolver valueResolver) {
        this.valueResolver = valueResolver;
    }

    public List<String> extract(MethodInvocationTree call, ArgContext argContext, MethodMeta methodMeta) {
        try {
            List<String> result = new ArrayList<>();
            String command = MethodTreeUtil.getMethodName(call);
            boolean isCondition = CONDITION_COMMANDS.contains(command);

            List<? extends ExpressionTree> arguments = call.getArguments();
            ArgValidator validator = new ArgValidator();

            for (int i = 0; i < arguments.size(); i++) {
                ExpressionTree arg = arguments.get(i);
                boolean quoteString = isCondition && i == 2;

                String firstColumn = valueResolver.resolve(arguments.get(0), argContext, false, false);
                validator.saveFirstArgInfoIfMatched(firstColumn, command, i);
                validator.validateArgIfMatched(i, inferLiteralType(arg));

                String resolved = valueResolver.resolve(arg, argContext, quoteString, false);
                result.add(resolved != null ? resolved : "");
            }
            return result;
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    private String inferLiteralType(ExpressionTree arg) {
        if (!(arg instanceof LiteralTree)) return null;
        return TypeInferrer.inferFromLiteralValue(((LiteralTree) arg).getValue());
    }
}