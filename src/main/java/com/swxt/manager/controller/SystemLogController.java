package com.swxt.manager.controller;

import com.swxt.manager.config.Core;
import com.swxt.manager.dto.PageResult;
import com.swxt.manager.dto.Result;
import com.swxt.manager.entity.SystemLog;
import com.swxt.manager.service.SystemLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统日志查询控制器（仅管理员）
 */
@RestController
@RequestMapping("/system-logs")
@PreAuthorize("hasRole('ADMIN')")
public class SystemLogController {

    private final SystemLogService systemLogService;

    public SystemLogController(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    /**
     * 分页查询系统日志
     */
    @GetMapping
    public Result<PageResult<SystemLog>> list(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "actionType", required = false) String actionType,
            @RequestParam(value = "targetType", required = false) String targetType,
            @RequestParam(value = "result", required = false) String result,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResult<SystemLog> pageResult = systemLogService.listLogsPage(
                username, actionType, targetType, result, page, size);
        return Result.success(pageResult);
    }

    /** 清空全部日志 */
    @DeleteMapping
    public Result<Void> clearLog() {
        systemLogService.clearLog();
        systemLogService.record(Core.ActionType.DELETE, Core.TargetType.LOG, Core.LogResult.SUCCESS);
        return Result.success();
    }
}
