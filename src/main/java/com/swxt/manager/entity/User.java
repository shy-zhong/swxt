package com.swxt.manager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swxt.manager.config.Core;
import lombok.Data;

/**
 * 用户实体
 */
@Data
public class User {

    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String wechatOpenid;
    private Core.Role role;
    private Integer status;
    private Integer deleted;
    private String accessToken;
}
