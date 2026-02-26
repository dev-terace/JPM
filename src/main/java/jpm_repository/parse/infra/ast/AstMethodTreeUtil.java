package jpm_repository.parse.infra.ast;

import com.sun.source.tree.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MethodInvocationTree / MemberSelectTree 에서 이름과 체인을 추출하는 유틸.
 */
public class AstMethodTreeUtil {

    private AstMethodTreeUtil() {}

    public static String getMethodName(MethodInvocationTree call) {
        ExpressionTree sel = call.getMethodSelect();


        if (sel instanceof IdentifierTree)  return ((IdentifierTree) sel).getName().toString();
        if (sel instanceof MemberSelectTree) return ((MemberSelectTree) sel).getIdentifier().toString();
        return "";
    }

    public static String getScopeName(MethodInvocationTree call) {
        ExpressionTree sel = call.getMethodSelect();
        if (sel instanceof MemberSelectTree) {
            ExpressionTree scope = ((MemberSelectTree) sel).getExpression();
            if (scope instanceof IdentifierTree)  return ((IdentifierTree) scope).getName().toString();
            if (scope instanceof MemberSelectTree) return ((MemberSelectTree) scope).getIdentifier().toString();
        }
        return null;
    }

    /**
     * a.select().from().where() 형태의 체이닝을 실행 순서(왼→오른) 리스트로 평탄화합니다.
     */
    public static List<MethodInvocationTree> flattenChain(ExpressionTree expr) {
        List<MethodInvocationTree> chain = new ArrayList<>();
        ExpressionTree current = expr;
        while (current instanceof MethodInvocationTree) {
            chain.add((MethodInvocationTree) current);
            ExpressionTree sel = ((MethodInvocationTree) current).getMethodSelect();
            if (sel instanceof MemberSelectTree) {
                current = ((MemberSelectTree) sel).getExpression();
            } else {
                break;
            }
        }
        Collections.reverse(chain);
        return chain;
    }
}