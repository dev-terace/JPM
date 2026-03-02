package io.jpm.common.utils;



import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class LogPrinter {

    private static Messager messager;






    // 프로세서 시작할 때 딱 한 번 호출
    public static void init(ProcessingEnvironment env) {
        messager = env.getMessager();
    }

    // 1. 일반 로그 (NOTE)
    public static void info(String message) {
        if (messager != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "Build Log: " + message);
        }
    }


    public static void exceptionInfo(Exception e) {
        // 스택 트레이스에서 에러가 발생한 지점 추출
        StackTraceElement[] stackTrace = e.getStackTrace();



        if (stackTrace != null && stackTrace.length > 0) {
            StackTraceElement element = stackTrace[0];

            String className = element.getClassName();   // 클래스명
            String methodName = element.getMethodName(); // 메서드명
            int lineNumber = element.getLineNumber();    // 라인 번호
            String errorMessage = (e.getMessage() != null) ? e.getMessage() : e.toString();

            // 가독성을 위해 포맷팅하여 출력
            String logMessage = String.format(
                    "[ERROR] 위치: %s.%s(Line: %d) | 사유: %s",
                    className, methodName, lineNumber, errorMessage
            );

            // 실제 로그 출력 실행 (여기서 System.out이나 다른 로거를 호출)
            info(logMessage);
        } else {
            info("[ERROR] 에러 정보를 추출할 수 없습니다: " + e);
        }
    }

    // 2. 경고 (WARNING) - 소스코드 위치 포함
    public static void warn(String message, Element element) {
        if (messager != null) {
            messager.printMessage(Diagnostic.Kind.WARNING, message, element);
        }
    }

    public static void error(String msg) {
        String formatted = String.format("\n"+msg);
        // Messager를 사용한다면 Diagnostic.Kind.ERROR와 함께 출력
        messager.printMessage(Diagnostic.Kind.ERROR, formatted);
    }

    public static void warn(String msg) {

        messager.printMessage(Diagnostic.Kind.WARNING, msg);
    }


}

