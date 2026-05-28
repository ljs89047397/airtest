package kr.go.molit.icas.common.exception;

import lombok.Getter;

/**
 * 비즈니스 예외 — 사용자에게 한국어 메시지 + 영문 code + HTTP status 전달.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final int    status;

    public BusinessException(String code, String message) {
        this(code, message, 400);
    }

    public BusinessException(String code, String message, int status) {
        super(message);
        this.code   = code;
        this.status = status;
    }

    public static BusinessException notFound(String entity) {
        return new BusinessException("NOT_FOUND", entity + " 정보를 찾을 수 없습니다.", 404);
    }

    public static BusinessException forbidden(String reason) {
        return new BusinessException("FORBIDDEN", reason, 403);
    }

    public static BusinessException badRequest(String reason) {
        return new BusinessException("BAD_REQUEST", reason, 400);
    }

    public static BusinessException conflict(String reason) {
        return new BusinessException("CONFLICT", reason, 409);
    }
}
