package com.swxt.manager.dto.register;

import lombok.Data;


@Data
public class RegisterResponse {

    
    private String username;

/**
 * 构造注册响应：注册成功的用户名
 */
    public RegisterResponse(String username) {
        this.username = username;
    }
}
