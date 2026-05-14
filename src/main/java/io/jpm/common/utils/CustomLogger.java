package io.jpm.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

public class CustomLogger {

    public static volatile boolean DEBUG_MODE = true;

    private static final BlockingQueue<String> logQueue = new ArrayBlockingQueue<>(8192);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static final String RESET  = "\u001B[0m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String BLUE   = "\u001B[34m";

    private final String className;

    static {
        Thread logThread = new Thread(() -> {
            while (true) {
                try {
                    System.out.println(logQueue.take());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        logThread.setDaemon(true);
        logThread.setPriority(Thread.MAX_PRIORITY);
        logThread.start();
    }

    private CustomLogger(Class<?> clazz) {
        this.className = clazz.getName();
    }

    public static CustomLogger getLogger(Class<?> clazz) {
        return new CustomLogger(clazz);
    }

    // {} 치환 핵심 메서드
    private static String format(String message, Object... args) {
        if (args == null || args.length == 0) return message;

        StringBuilder sb = new StringBuilder(message.length() + 32);
        int argIndex = 0;
        int i = 0;

        while (i < message.length()) {
            if (i < message.length() - 1
                    && message.charAt(i) == '{'
                    && message.charAt(i + 1) == '}') {
                // {} 발견 → 인자값으로 치환
                if (argIndex < args.length) {
                    sb.append(args[argIndex++]);
                } else {
                    sb.append("{}"); // 인자 부족하면 그대로
                }
                i += 2;
            } else {
                sb.append(message.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }

    private void log(String level, String color, String message) {
        String time = LocalDateTime.now().format(formatter);
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];

        String clickable = "at "
                + caller.getClassName()
                + "."
                + caller.getMethodName()
                + "("
                + caller.getFileName()
                + ":"
                + caller.getLineNumber()
                + ")";

        String formatted = new StringBuilder(128)
                .append(color)
                .append(time)
                .append(" [").append(level).append("] --- ")
                .append(clickable)
                .append("\n")
                .append(message)
                .append(RESET)
                .append("\n")
                .toString();

        logQueue.offer(formatted);
    }

    // DEBUG
    public void debug(String message)                       { if (!DEBUG_MODE) return; log("DEBUG", BLUE,   message); }
    public void debug(String message, Object... args)       { if (!DEBUG_MODE) return; log("DEBUG", BLUE,   format(message, args)); }
    public void debug(Supplier<String> msgSupplier)         { if (!DEBUG_MODE) return; log("DEBUG", BLUE,   msgSupplier.get()); }

    // INFO
    public void info(String message)                        { log("INFO ", GREEN,  message); }
    public void info(String message, Object... args)        { log("INFO ", GREEN,  format(message, args)); }

    // WARN
    public void warn(String message)                        { log("WARN ", YELLOW, message); }
    public void warn(String message, Object... args)        { log("WARN ", YELLOW, format(message, args)); }

    // ERROR
    public void error(String message)                       { log("ERROR", RED,    message); }
    public void error(String message, Object... args)       { log("ERROR", RED,    format(message, args)); }
}
