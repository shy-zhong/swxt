package com.swxt.manager.dto.user;

import com.swxt.manager.config.Core;
import lombok.Data;

/**
 * 管理员更新用户请求参数
 *
 * <p>用于修改用户基本信息、角色权限与状态；密码不在此处修改。</p>
 */
@Data
public class UpdateUserRequest {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String wechatOpenid;
    private Core.Role role;
    private Integer status;
}
