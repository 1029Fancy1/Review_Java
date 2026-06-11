package com.knowledgehub.exception;

import com.knowledgehub.common.ErrorCode;
import com.knowledgehub.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * 学习要点（Day 1 重点理解）：
 * 1. @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 *    —— 拦截所有 @Controller 和 @RestController 抛出的异常
 *    —— 返回 JSON 而不是视图页面
 *
 * 2. @ExceptionHandler 的执行流程：
 *    Controller 抛出异常
 *    → Spring 查找匹配的 @ExceptionHandler 方法
 *    → 优先匹配最具体的异常类型
 *    → 执行异常处理方法 → 返回 Result
 *
 * 3. 为什么需要全局异常处理？
 *    —— Controller 中不需要写 try-catch
 *    —— 统一返回格式（Result）
 *    —— 避免异常信息直接暴露给前端
 *
 * 4. @ResponseStatus 的作用？
 *    —— 设置 HTTP 响应状态码
 *    —— 前端可根据状态码做不同处理（如 401 跳转登录页）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     *
     * 触发场景：
     * - throw new BusinessException(ErrorCode.USER_NOT_FOUND)
     * - throw new BusinessException(ErrorCode.PARAM_ERROR, "具体错误信息")
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("[业务异常] URI: {} | code: {} | message: {}",
                request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常 —— @Valid 校验失败时抛出
     *
     * 触发场景：
     * - @NotNull / @NotBlank / @Size 等校验注解校验失败
     *
     * TODO: 请你手敲理解这个方法的处理逻辑。
     *
     * 学习目标：
     * 1. 理解 MethodArgumentNotValidException 的结构
     * 2. 理解如何提取校验失败的具体字段和错误信息
     *
     * 参考实现：
     * MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
     * String message = ex.getBindingResult().getFieldErrors()
     *     .stream()
     *     .map(f -> f.getField() + ": " + f.getDefaultMessage())
     *     .collect(Collectors.joining(", "));
     * return Result.fail(ErrorCode.PARAM_ERROR, message);
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("[参数校验失败] {}", message);
        return Result.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 绑定异常 —— 表单参数绑定失败时抛出
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String message = e.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[参数绑定失败] {}", message);
        return Result.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 缺少请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingParamException(MissingServletRequestParameterException e) {
        log.warn("[缺少请求参数] {}", e.getMessage());
        return Result.fail(ErrorCode.PARAM_ERROR, "缺少必要参数: " + e.getParameterName());
    }

    /**
     * 文件上传大小超出限制
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMaxUploadSizeException(MaxUploadSizeExceededException e) {
        log.warn("[文件上传超限] {}", e.getMessage());
        return Result.fail(ErrorCode.FILE_UPLOAD_ERROR, "文件大小超出限制");
    }

    /**
     * 兜底异常 —— 处理所有未被上面捕获的异常
     *
     * 为什么放在最后？
     * Spring 按异常类型匹配，越具体的异常越优先匹配。
     * Exception.class 是最宽泛的，放在最后作为兜底。
     *
     * 注意：这里不要返回 e.getMessage() 给前端，可能泄露敏感信息。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("[系统异常] URI: {} | 异常类型: {} | 异常信息: {}",
                request.getRequestURI(), e.getClass().getName(), e.getMessage(), e);
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }
}
