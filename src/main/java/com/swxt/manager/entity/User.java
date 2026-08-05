package com.swxt.manager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swxt.manager.config.Core;
import lombok.Data;

/**
 * 用户实体，对应数据库表 user
 *
 * <p>包含账号信息、联系方式、微信 openid（预留）及角色权限。
 * password 字段使用 @JsonIgnore，序列化时不会返回给前端。</p>
 */
@Data
public class User {

    /** 用户 ID（自增主键） */
    private Long id;

    /** 登录用户名（唯一） */
    private String username;

    /** 密码（BCrypt 加密存储；@JsonIgnore 防止泄露到前端） */
    @JsonIgnore
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 微信 openid（预留字段，用于微信扫码登录绑定） */
    private String wechatOpenid;

    /** 角色：ADMIN=管理员，USER=普通用户 */
    private Core.Role role;

    /** 状态：0=禁用，1=启用 */
    private Integer status;

    /** 软删除：0=正常，1=已删除 */
    private Integer deleted;
}
