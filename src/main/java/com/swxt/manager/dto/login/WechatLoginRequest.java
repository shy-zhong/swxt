package com.swxt.manager.dto.login;

import lombok.Data;

/**
 * 微信网页授权登录请求
 */
@Data
public class WechatLoginRequest {

    /** 微信授权 code */
    private String code;
}
