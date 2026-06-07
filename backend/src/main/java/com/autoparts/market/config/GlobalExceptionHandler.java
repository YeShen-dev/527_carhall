package com.autoparts.market.config;

import com.autoparts.market.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 — 统一返回 ApiResponse 格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 (RuntimeException) → HTTP 400 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntime(RuntimeException e) {
        log.warn("[业务异常] {}", e.getMessage());
        return ApiResponse.error(400, e.getMessage());
    }

    /** 参数校验失败 → HTTP 400 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ":" + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("[参数校验] {}", msg);
        return ApiResponse.error(400, msg);
    }

    /** 兜底异常 → HTTP 500 */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnknown(Exception e) {
        log.error("[系统异常]", e);
        return ApiResponse.error(500, "服务器内部错误");
    }

    /** HTTP方法不匹配 → 405 */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleMethodNotAllowed(
            org.springframework.web.HttpRequestMethodNotSupportedException e) {
        log.warn("[方法不匹配] {} {} (supported: {})",
                e.getMethod(), e.getMessage(), e.getSupportedHttpMethods());
        return ApiResponse.error(405, "请求方式错误，仅支持 " + e.getSupportedHttpMethods());
    }

    /** 路径不存在 → 404 */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ApiResponse<Void> handleNotFound(
            org.springframework.web.servlet.resource.NoResourceFoundException e) {
        log.warn("[路径不存在] {}", e.getMessage());
        return ApiResponse.error(404, "接口路径不存在");
    }
}
