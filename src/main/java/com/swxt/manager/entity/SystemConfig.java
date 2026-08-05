package com.swxt.manager.entity;

import lombok.Data;

/**
 * 系统配置实体，对应数据库表 system_config
 *
 * <p>以 key-value 形式存储系统级配置，如网站背景图、分页大小、是否开启日志等。</p>
 */
@Data
public class SystemConfig {

    /** 配置 ID（自增主键） */
    private Long id;

    /** 配置键（唯一），如 homepage_background */
    private String configKey;

    /** 配置值（字符串存储，使用时按需转换类型） */
    private String configValue;

    /** 配置项说明 */
    private String description;
}
