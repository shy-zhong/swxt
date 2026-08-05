package com.swxt.manager.config;

import com.swxt.manager.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

/**
 * 处理业务异常：按枚举 code/message 返回
 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getResultCode().getCode(), e.getMessage());
        return Result.of(e.getResultCode());
    }

/**
 * 处理数据库约束冲突：重复数据返回 409，其余返回 400
 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("数据库约束冲突: {}", e.getMessage());
        String msg = e.getMessage();
        if (msg != null && msg.toLowerCase().contains("duplicate")) {
            return Result.error(409, "数据已存在，请勿重复提交");
        }
        return Result.error(400, "数据校验失败，请检查输入内容");
    }

/**
 * 处理参数校验失败：取第一个字段错误提示
 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("请求参数校验失败");
        log.warn("参数校验失败: {}", message);
        return Result.error(400, message);
    }

/**
 * 处理权限拒绝：返回 403（项目业务码 FORBIDDEN），替代此前被 RuntimeException 兜底成 500 的问题
 */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限拒绝: {}", e.getMessage());
        return Result.of(Core.ResultCode.FORBIDDEN);
    }

/**
 * 处理请求方法不支持（路径存在但方法不匹配，如 GET /products/{id} 仅有 DELETE）：返回 405
 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {} {}", e.getMethod(), e.getMessage());
        return Result.error(405, "请求方法不支持");
    }

/**
 * 处理未匹配路径（如 GET /products/{id} 不存在）：返回 404，替代此前被 Exception 兜底成 500 的问题
 */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return Result.of(Core.ResultCode.NOT_FOUND);
    }

/**
 * 运行时异常兜底：返回 500 并附带异常信息
 */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(500, e.getMessage());
    }

/**
 * 未知异常兜底：统一提示，不暴露内部细节
 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
