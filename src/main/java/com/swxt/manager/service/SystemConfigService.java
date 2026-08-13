package com.swxt.manager.service;

import com.swxt.manager.entity.SystemConfig;
import com.swxt.manager.mysql.SystemConfigMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class SystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigService.class);

    private final SystemConfigMapper systemConfigMapper;

    
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

/**
 * 构造配置服务：注入配置 Mapper
 */
    public SystemConfigService(SystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
    }

/**
 * 应用启动后初始化配置缓存
 */
    @PostConstruct
    public void initCache() {

        reloadCache();
    }

/**
 * 全量重新加载配置缓存
 */
    public void reloadCache() {
        cache.clear();
        List<SystemConfig> all = systemConfigMapper.listAll();
        for (SystemConfig c : all) {
            cache.put(c.getConfigKey(), c.getConfigValue());
        }
        log.info("系统配置缓存已加载，共 {} 项", cache.size());
        ensureDefault("show_disabled_products", "false", "是否展示已下架（状态为0）的商品");
    }

/**
 * 配置项缺失时自动补建默认值（首次启动或升级时生效）
 */
    private void ensureDefault(String key, String defaultValue, String description) {
        if (!cache.containsKey(key)) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(defaultValue);
            config.setDescription(description);
            systemConfigMapper.insert(config);
            cache.put(key, defaultValue);
            log.info("已自动补建配置项：{}", key);
        }
    }

/**
 * 读取布尔型配置：true/1 视为 true，其余（含缺失）视为 false
 */
    public boolean getSystemConfigBoolean(String key) {
        String res = cache.get(key);
        return "true".equalsIgnoreCase(res) || "1".equals(res);
    }

/**
 * 读取整数型配置，非法值或缺失返回 -1
 */
    public int getSystemConfigInt(String key) {
        String v = cache.get(key);
        if (v == null) return -1;
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
        String v = cache.get(key);
        if (v == null) return -1;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 [{}] 不是合法长整数，使用默认值 {}", key, v, -1);
            return -1;
        }
    }

/**
 * 判断配置键是否存在
 */
    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

/**
 * 查询全部配置项（供管理页面展示与前端拉取）
 */
    public List<SystemConfig> selectSystemConfig() {
        return systemConfigMapper.listAll();
    }

/**
 * 更新配置项并同步刷新缓存，返回是否更新成功
 */
    public boolean updateSystemConfig(SystemConfig systemConfig) {
        boolean ok = systemConfigMapper.update(systemConfig) > 0;
        if (ok) {
            cache.put(systemConfig.getConfigKey(), systemConfig.getConfigValue());
        }
        return ok;
    }
}
