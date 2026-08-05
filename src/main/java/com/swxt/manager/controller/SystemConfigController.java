package com.swxt.manager.controller;

import com.swxt.manager.config.Core;
import com.swxt.manager.dto.Result;
import com.swxt.manager.entity.SystemConfig;
import com.swxt.manager.service.SystemConfigService;
import com.swxt.manager.service.SystemLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/system-config")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;
    private final SystemLogService logService;

    /**
     * 构造系统配置控制器：注入配置服务与日志服务
     */
    public SystemConfigController(SystemConfigService systemConfigService, SystemLogService logService) {
        this.systemConfigService = systemConfigService;
        this.logService = logService;
    }

    /**
     * 查询全部配置项
     */
    @GetMapping
    public Result<List<SystemConfig>> list() {
        return Result.success(systemConfigService.selectSystemConfig());
    }

    /**
     * 查询全部配置项
     */
    @GetMapping("/public")
    public Result<List<SystemConfig>> publicList() {
        return Result.success(systemConfigService.selectSystemConfig());
    }

    /**
     * 批量更新配置项
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<String>> update(@RequestBody List<SystemConfig> configs) {
        List<String> errorList = new ArrayList<String>();
        for (SystemConfig config : configs) {
            boolean updated = systemConfigService.updateSystemConfig(config);
            logService.record(Core.ActionType.UPDATE, Core.TargetType.CONFIG, config.getId(),
                    updated ? Core.LogResult.SUCCESS : Core.LogResult.FAIL);
            if (!updated) {
                errorList.add(config.getConfigKey());
            }
        }
        if (errorList.isEmpty()) {
            return Result.success();
        } else {
            Result<List<String>> result = Result.error(405, "错误的配置项");
            result.setData(errorList);
            return result;
        }
    }
}
