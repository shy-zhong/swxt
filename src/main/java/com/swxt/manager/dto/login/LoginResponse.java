package com.swxt.manager.dto.login;

import lombok.Data;


@Data
public class LoginResponse {

    
    private String token;

    
    private String username;

    
    private String role;

/**
 * 构造登录响应：令牌、用户名与角色
 */
    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }
}
