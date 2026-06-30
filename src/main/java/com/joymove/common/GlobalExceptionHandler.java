package com.joymove.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 按 继承树从具体到兜底 排列，确保每种异常返回正确的{@link Result} JSON
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 400 系列 — 客户端请求错误 ====================

    /**
     * 简单参数校验失败 — @Validated + @NotBlank/@Size 等在 @RequestParam 上
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return Result.error(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 对象参数校验失败 — @Valid + @RequestBody 的嵌套校验
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * GET 请求参数绑定到对象失败 — 如 /api/xxx?age=abc 绑定到 Integer age
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 参数类型转换失败 — 如 projectId 传了空字符串 ""
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error(ResultCode.BAD_REQUEST.getCode(),
                "参数错误：" + e.getName() + " 格式不正确");
    }

    /**
     * 缺少必填参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.error(ResultCode.BAD_REQUEST.getCode(),
                "缺少必填参数：" + e.getParameterName());
    }

    /**
     * @RequestBody JSON 格式错误 / 字段类型不匹配
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误，请检查 JSON 数据");
    }

    /**
     * 上传文件超过限制大小
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "文件过大，单张图片不超过 2MB");
    }

    // ==================== 403 — 权限不足 ====================

    /**
     * Spring Security 权限拒绝 — 如非 ADMIN 访问 /admin/**
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDenied(AccessDeniedException e) {
        return Result.error(ResultCode.FORBIDDEN);
    }

    // ==================== 405 — 请求方法不支持 ====================

    /**
     * HTTP 方法错误 — 如 POST 接口用了 GET
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error(ResultCode.METHOD_NOT_ALLOWED.getCode(),
                "不支持 " + e.getMethod() + " 请求，请使用 " + e.getSupportedHttpMethods());
    }

    // ==================== 业务异常 ====================

    /**
     * 业务异常 — 手动抛出的 BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // ==================== 500 兜底 ====================

    /**
     * 其他未捕获异常 — 打印完整堆栈，返回通用错误信息
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.ERROR);
    }
}
