package com.swxt.manager.dto.system_config;

import com.swxt.manager.entity.SystemConfig;
import lombok.Data;

import java.util.List;


@Data
public class SystemConfigResponse {

    
    private List<SystemConfig> systemConfigs;

/**
 * 构造系统配置响应：配置项列表
 */
    public SystemConfigResponse(List<SystemConfig> systemConfigs) {
        this.systemConfigs = systemConfigs;
    }
}
