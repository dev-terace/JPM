package utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class MParserUtils {

    // 이제 타겟은 오직 하나입니다.
    private static final String TARGET_TYPE = "MField";

    public static class Pair extends mq_mapper.infra.repo.Pair {
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

    public static List<List<Pair>> execute(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) throw new Exception("File not found: " + filePath);

        FileInputStream in = new FileInputStream(file);
        CompilationUnit cu = StaticJavaParser.parse(in);
        List<List<Pair>> result = new ArrayList<>();

        cu.findAll(VariableDeclarator.class).forEach(v -> {
            // 1. 변수 타입이 "MVariable" 인지 확인
            if (TARGET_TYPE.equals(v.getType().asString())) {
                List<Pair> columnInfo = new ArrayList<>();

                // 2. 변수명 저장 (기본 컬럼명으로 사용됨)
                columnInfo.add(new Pair("fieldName", v.getNameAsString()));

                // 3. 메서드 체인 파싱 (.builder().type(...).build())
                v.getInitializer().ifPresent(expr -> {
                    parseMethodChain(expr, columnInfo);
                });

                // 체인 순서가 거꾸로 들어갈 수 있으므로 정렬이 필요하면 여기서 처리
                // (지금 구조에선 순서가 크게 상관없음)
                result.add(columnInfo);
            }
        });

        return result;
    }














    // 재귀적으로 메서드 체인을 파고들어 정보를 추출
    private static void parseMethodChain(Expression expr, List<Pair> info) {
        if (expr instanceof MethodCallExpr) {
            MethodCallExpr methodCall = (MethodCallExpr) expr;
            String methodName = methodCall.getNameAsString();

            // builder()와 build()는 제외하고 실제 설정값만 추출
            if (!"builder".equals(methodName) && !"build".equals(methodName)) {
                String argValue = extractArgValue(methodCall);
                // 키(메서드명)와 값(인자) 저장


                info.add(new Pair(methodName, argValue));
            }

            // 다음 체인(Scope)으로 이동 (재귀)
            methodCall.getScope().ifPresent(scope -> parseMethodChain(scope, info));
        }
    }

    // 🔥 핵심: 인자 타입별 값 추출 로직 (Enum, Class, String 등)
    private static String extractArgValue(MethodCallExpr methodCall) {
        if (methodCall.getArguments().isEmpty()) return "";

        Expression arg = methodCall.getArgument(0);

        if (arg.isStringLiteralExpr()) {
            return arg.asStringLiteralExpr().getValue(); // "문자열" -> 문자열
        }
        else if (arg.isBooleanLiteralExpr()) {
            return String.valueOf(arg.asBooleanLiteralExpr().getValue()); // true -> "true"
        }
        else if (arg.isIntegerLiteralExpr()) {
            return arg.asIntegerLiteralExpr().getValue(); // 100 -> "100"
        }
        else if (arg.isFieldAccessExpr()) {
             // Enum 처리: ColumnType.STRING -> "STRING" 추출
            return arg.asFieldAccessExpr().getNameAsString();
        }
        else if (arg.isClassExpr()) {
            // Class 처리: UserEntity.class -> "UserEntity" 추출
            return arg.asClassExpr().getType().asString();
        }
        else {
            // 그 외 (상수나 변수 등) -> 텍스트 그대로 반환
            return arg.toString();
        }
    }
}