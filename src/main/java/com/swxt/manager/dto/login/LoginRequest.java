package com.swxt.manager.dto.login;

import lombok.Data;

/**
 * 登录请求参数
 *
 * username 支持用户名或用户 ID（字符串形式），password 为明文密码，
 * 由后端进行 BCrypt 校验。
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
