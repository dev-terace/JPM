package io.jpm.core.m_entity.parse.infra.ast;



import com.sun.source.tree.*;
import com.sun.source.util.Trees;

import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

public class AstMFieldParserV2 {

    public static class Pair extends io.jpm.common.utils.Pair {
        public String key;
        public String value;
        public Pair(String key, String value) { this.key = key; this.value = value; }
        @Override public String toString() { return key + "=" + value; }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Annotation Processor 내부에서 호출되는 진입점
     * @param fieldElement 프로세서가 찾은 필드 엘리먼트 (VariableElement)
     * @param trees 프로세서 환경에서 얻은 Trees 인스턴스
     */
    public static List<Pair> extractFieldInfo(VariableElement fieldElement, Trees trees) {
        List<Pair> columnInfo = new ArrayList<>();

        // 1. Element로부터 컴파일러가 만들어둔 구문 트리(AST)를 가져옵니다.
        Tree pathTree = trees.getTree(fieldElement);

        if (pathTree instanceof VariableTree) {
            VariableTree varTree = (VariableTree) pathTree;

            // 2. 변수명 저장 (기본 컬럼명)
            columnInfo.add(new Pair("fieldName", varTree.getName().toString()));

            // 3. 변수 초기화 블록 (.builder()...) 가져오기
            ExpressionTree initializer = varTree.getInitializer();
            if (initializer != null) {
                parseMethodChain(initializer, columnInfo);
            }
        }


        return columnInfo;
    }

    // 재귀적으로 메서드 체인을 파고들어 정보를 추출
    private static void parseMethodChain(ExpressionTree expr, List<Pair> info) {
        // JavaParser의 MethodCallExpr에 해당
        if (expr instanceof MethodInvocationTree) {
            MethodInvocationTree methodCall = (MethodInvocationTree) expr;
            ExpressionTree methodSelect = methodCall.getMethodSelect();

            // 메서드 호출부 확인 (예: obj.methodName)
            if (methodSelect instanceof MemberSelectTree) {
                MemberSelectTree memberSelect = (MemberSelectTree) methodSelect;
                String methodName = memberSelect.getIdentifier().toString();

                // builder()와 build()는 제외하고 실제 설정값만 추출
                if (!"builder".equals(methodName) && !"build".equals(methodName)) {

                    String argValue = extractArgValue(methodCall);

                    info.add(new Pair(methodName, argValue));
                }

                // 다음 체인(Scope)으로 이동 (재귀)
                // memberSelect.getExpression()이 JavaParser의 getScope() 역할을 합니다.
                parseMethodChain(memberSelect.getExpression(), info);
            }
        }
    }

    // 🔥 핵심: 인자 타입별 값 추출 로직 (Tree API 방식)
    private static String extractArgValue(MethodInvocationTree methodCall) {
        if (methodCall.getArguments().isEmpty()) return "";

        ExpressionTree arg = methodCall.getArguments().get(0);

        // 1. Literal 처리 (String, Integer, Boolean 모두 포함)
        if (arg instanceof LiteralTree) {
            Object value = ((LiteralTree) arg).getValue();
            return value != null ? value.toString() : "null";
        }
        // 2. Enum 상수나 클래스 타입 (.class) 처리
        else if (arg instanceof MemberSelectTree) {
            MemberSelectTree memberSelect = (MemberSelectTree) arg;
            String identifier = memberSelect.getIdentifier().toString();

            if ("class".equals(identifier)) {
                // Class 처리: UserEntity.class -> "UserEntity" 반환
                return memberSelect.getExpression().toString();
            } else {
                // Enum 처리: ColumnType.STRING -> "STRING" 반환
                return identifier;
            }
        }
        // 3. 단순 식별자 (변수 등)
        else if (arg instanceof IdentifierTree) {
            return ((IdentifierTree) arg).getName().toString();
        }

        // 그 외
        return arg.toString();
    }
}
