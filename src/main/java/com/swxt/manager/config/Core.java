package com.swxt.manager.config;


public class Core {

    
    public enum Role {
        ADMIN,
        OPERATOR,
        USER
    }

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        REGISTER
    }

    public enum TargetType {
        USER,
        PRODUCT,
        CATEGORY,
        STOCK,
        ORDER,
        CONFIG,
        LOG
    }

    public enum LogResult {
        SUCCESS,
        FAIL
    }

    
    public enum ResultCode {

        
        SUCCESS(200, "success", true),

        
        BAD_REQUEST(400, "请求参数错误", false),
        UNAUTHORIZED(401, "未登录或登录已过期", false),
        FORBIDDEN(403, "无权限访问", false),
        NOT_FOUND(404, "资源不存在", false),
        SERVER_ERROR(500, "服务器内部错误", false),

        
        REGISTER_CLOSED(1001, "系统已关闭注册", false),
        PASSWORD_TOO_SHORT(1002, "密码长度不符合要求", false),
        LOGIN_FAILED(1003, "用户名或密码错误", false),
        USERNAME_EXISTS(1004, "用户名已存在", false),
        CONFIG_INVALID(1005, "配置项格式非法", false);

        private final int code;
        private final String message;
        private final boolean success;

        ResultCode(int code, String message, boolean success) {
            this.code = code;
            this.message = message;
            this.success = success;
        }

/**
 * 获取状态码
 */
        public int getCode() {
            return code;
        }

/**
 * 获取提示信息
 */
        public String getMessage() {
            return message;
        }

/**
 * 是否成功
 */
        public boolean isSuccess() {
            return success;
        }
    }
}
