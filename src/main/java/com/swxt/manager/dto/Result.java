package com.swxt.manager.dto;

import com.swxt.manager.config.Core;
import lombok.Data;


@Data
public class Result<T> {

    
    private int code;

    
    private String message;

    
    private boolean success;

    
    private T data;

/**
 * 成功返回：携带数据，状态码 200
 */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(Core.ResultCode.SUCCESS.getCode());
        result.setMessage(Core.ResultCode.SUCCESS.getMessage());
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

/**
 * 成功返回：携带数据，状态码 200
 */
    public static <T> Result<T> success() {

        return success(null);
    }

/**
 * 失败返回：自定义提示信息（默认 500）
 */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(Core.ResultCode.SERVER_ERROR.getCode());
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

/**
 * 失败返回：自定义提示信息（默认 500）
 */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

/**
 * 按枚举构造返回结果，成功/失败与提示信息取自枚举定义
 */
    public static <T> Result<T> of(Core.ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setSuccess(resultCode.isSuccess());
        return result;
    }

/**
 * 按枚举构造返回结果，成功/失败与提示信息取自枚举定义
 */
    public static <T> Result<T> of(Core.ResultCode resultCode, T data) {
        Result<T> result = of(resultCode);
        result.setData(data);
        return result;
    }
}
