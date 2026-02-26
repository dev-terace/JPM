package exception;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import config.AppConfig;
import exception.cache.domain.SourceLocationCache;
import exception.cache.domain.vo.ChainSourceLocation;
import org.gradle.api.GradleException;
import utils.LogPrinter;

import javax.lang.model.element.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

//싱글톤 처리
//나중에 doc 주소 붙일 수도 있음
public class ErrorCollector {
    private static final SourceLocationCache cache = AppConfig.getSourceLocationCache();
    // 현재 수집 중인 임시 상태값들 (Buffer)
    private static ErrorCode err;
    private static String className;


    private static String methodName;
    private static String chainMethodName;

    private static Element errorElement;
    private static Trees trees;
    private static final List<ErrorInfo> errorInfos = new ArrayList<>();
    private static String expression;

    // --- 각 도메인에서 개별적으로 호출할 Setter들 (Chaining 지원) ---
    public static void addErrorInfo(ErrorCode err) {
        setErr(err);
        errorInfos.add(ErrorInfo.builder()
                        .chainMethodName(chainMethodName)
                        .errorElement(errorElement)
                        .className(className)
                        .err(err)
                        .methodName(methodName)
                        .expression(expression)
                .build());
    }

    public static void setErr(ErrorCode code)    { err = code; }

    public static void setClassName(String name) {
        className = name;
    }


    public static void setMethodName(String name)      { methodName = name; }
    public static void setChainMethodName(String name) { chainMethodName = name; }


    public static void setTrees(Trees trees) {
        ErrorCollector.trees = trees;
    }

    public ErrorCode getErr() {
        return err;
    }

    public String getClassName() {
        return className;
    }




    public String getMethodName() {
        return methodName;
    }

    public String getChainMethodName() {
        return chainMethodName;
    }



    public Element getErrorElement() {
        return errorElement;
    }

    /**
     * 현재까지 쌓인 값들을 하나의 에러 객체로 확정하여 리스트에 추가
     */




    public static String reportAll() {
        if(errorInfos.isEmpty()){return null;}
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("\n================================================================================");
            sb.append("\n[JPM ERROR REPORT]");
            sb.append("\n================================================================================");

            for(ErrorInfo e : errorInfos) {

                ChainSourceLocation loc = null;
                try {
                    // 캐시에서 위치 정보 조회
                    loc = cache.popChainSourceLocation(e.getClassName(), e.getMethodName(), e.getChainMethodName());
                    if (loc != null) {
                        errorElement = loc.getElement();
                        expression = loc.getExpression();
                    }
                } catch (Exception ignored) {
                    // 캐시 조회 실패 시 조용히 넘어감
                }

                // null 안전하게 처리

                String code = (e.getErr() != null && e.getErr().getCode() != null) ? e.getErr().getCode() : "UNKNOWN";
                String desc = (e.getErr() != null && e.getErr().getMessage() != null) ? e.getErr().getMessage() : "";

                sb.append("\n\n▶ Error: ").append(code);
                sb.append("\n  Description: ").append(desc);
                sb.append("\n  Target: repository[").append(e.getClassName()).append("], Method[").append(e.getMethodName()).append("]");


                if (loc != null) {
                    // ✅ IDE 클릭 링크 헬퍼 메서드만 깔끔하게 호출
                    String ideLink = buildAbsoluteLink(errorElement, loc.getLineNumber());

                    sb.append("\n").append(ideLink);

                    sb.append("\n  Details: Failed at '").append(String.format("(%s)'", expression));
                } else {
                    sb.append("Location not found for ")
                            .append(e.getClassName()).append(".")
                            .append(e.getMethodName()).append("\n")
                            .append(e.getChainMethodName()).append("\n");

                }
                sb.append("\n------------------------------------------------------------\n");

                // 한 번에 출력

            }


        return sb.toString();

        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new GradleException(e.getMessage());
        }


    }


    private static String buildAbsoluteLink(Element element, long lineNumber) {
        if (element == null) return "Location Unknown";

        try {
            // 1. Element에서 TreePath 획득
            TreePath path = trees.getPath(element);
            if (path == null) return "Location Unknown";

            // 2. 소스 파일 객체(JavaFileObject) 가져오기
            File file = new File(path.getCompilationUnit().getSourceFile().toUri());
            String absolutePath = file.getAbsolutePath();

            // IDE가 'at 클래스.메서드(경로:줄)' 패턴으로 인식하도록 유도
            // 여기서 'JPM.error'는 IDE를 속이기 위한 가짜 경로입니다.


            return String.format("  at Click.error(%s:%d)", absolutePath, lineNumber);



        } catch (Exception e) {
            return "Path Error: " + e.getMessage();
        }
    }

    private static void logPrint(String msg) {
        LogPrinter.error(msg);
    }



}