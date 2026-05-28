package kr.go.molit.icas.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 표준 API 응답.
 *
 * <pre>
 * {
 *   "success": true,
 *   "code":    "OK",
 *   "message": "처리되었습니다",
 *   "data":    { ... },
 *   "traceId": "abc-123"
 * }
 * </pre>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String  code;
    private final String  message;
    private final T       data;
    private final String  traceId;

    private ApiResponse(boolean success, String code, String message, T data, String traceId) {
        this.success = success;
        this.code    = code;
        this.message = message;
        this.data    = data;
        this.traceId = traceId;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", null, data, org.slf4j.MDC.get("traceId"));
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, "OK", message, data, org.slf4j.MDC.get("traceId"));
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null, org.slf4j.MDC.get("traceId"));
    }

    public static <T> ApiResponse<T> fail(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data, org.slf4j.MDC.get("traceId"));
    }
}
