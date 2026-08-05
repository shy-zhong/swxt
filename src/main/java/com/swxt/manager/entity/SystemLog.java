package com.swxt.manager.entity;

import lombok.Data;

/**
 * 系统操作日志实体，对应数据库表 system_log
 *
 * <p>记录用户操作：时间 + 用户ID/用户名 + 操作类型 + 操作对象 + 结果 + IP。</p>
 */
@Data
public class SystemLog {

    /** 日志 ID（自增主键） */
    private Long id;

    /** 操作用户名 */
    private String username;

    /** 操作用户ID */
    private Long userId;

    /** 操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT */
    private String actionType;

    /** 操作对象类型：USER/PRODUCT/CATEGORY/STOCK/CONFIG */
    private String targetType;

    /** 操作对象ID */
    private Long targetId;

    /** 操作结果：SUCCESS/FAIL */
    private String result;

    /** 客户端 IP 地址 */
    private String ip;

    /** 操作时间（yyyy-MM-dd HH:mm:ss） */
    private String createdAt;
}
