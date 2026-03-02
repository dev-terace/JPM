package io.jpm.common.exception;

public class CustomProcessorException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomProcessorException(ErrorCode errorCode) {
        // 부모 예외 메시지로 에러 상세 내용을 미리 포맷팅하여 넘깁니다.
        super(String.format("[%s]\n",
                errorCode.getCode()));
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }

}