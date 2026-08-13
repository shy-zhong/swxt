package com.swxt.manager.service;

import com.swxt.manager.entity.SystemConfig;
import com.swxt.manager.mysql.SystemConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigService.class);

    private final SystemConfigMapper systemConfigMapper;

    public SystemConfigService(SystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
    }

    public boolean getSystemConfigBoolean(String key) {
        SystemConfig config = systemConfigMapper.getByKey(key);
        if (config == null || config.getConfigValue() == null) {
            return false;
        }
        String value = config.getConfigValue();
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    public int getSystemConfigInt(String key) {
        SystemConfig config = systemConfigMapper.getByKey(key);
        if (config == null || config.getConfigValue() == null) {
            return -1;
        }
        String v = config.getConfigValue().trim();
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 [{}] 不是合法整数，使用默认值 {}", key, v, -1);
            return -1;
        }
    }

/**
 * 读取长整数型配置，非法值或缺失返回 -1
 */
    public long getLong(String key) {
        SystemConfig config = systemConfigMapper.getByKey(key);
        if (config == null || config.getConfigValue() == null) {
            return -1;
        }
        String v = config.getConfigValue().trim();
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 [{}] 不是合法长整数，使用默认值 {}", key, v, -1);
            return -1;
        }
    }

/**
 * 判断配置键是否存在
 */
    public boolean containsKey(String key) {
        return systemConfigMapper.getByKey(key) != null;
    }

/**
 * 查询全部配置项（供管理页面展示与前端拉取）
 */
    public List<SystemConfig> selectSystemConfig() {
        return systemConfigMapper.listAll();
    }

/**
 * 更新配置项，返回是否更新成功
 */
    public boolean updateSystemConfig(SystemConfig systemConfig) {
        return systemConfigMapper.update(systemConfig) > 0;
    }
}
