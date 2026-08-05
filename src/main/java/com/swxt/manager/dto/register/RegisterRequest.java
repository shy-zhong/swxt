package com.swxt.manager.dto.register;

import com.swxt.manager.config.Core;
import lombok.Data;

/**
 * 注册请求参数
 *
 * 其中 role 由前端传入但默认应为 USER；管理员通常通过后台创建，
 * wechatOpenid 预留用于微信绑定场景。
 */
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private Core.Role role;
    private String realName;
    private String phone;
    private String email;
    private String wechatOpenid;
    private Integer status;
}
