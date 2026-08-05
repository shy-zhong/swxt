package com.swxt.manager.service;

import com.swxt.manager.config.Core;
import com.swxt.manager.dto.PageResult;
import com.swxt.manager.entity.SystemLog;
import com.swxt.manager.mysql.SystemLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统操作日志服务
 *
 * <p>统一从 Spring Security 上下文取操作人（用户名 + userId），从 HttpServletRequest 取 IP，
 * 由 Controller 在关键操作完成后调用 record(...) 写入 system_log 表。</p>
 */
@Slf4j
@Service
public class SystemLogService {

    @Autowired
    private SystemLogMapper systemLogMapper;

    @Autowired
    private HttpServletRequest request;

    /**
     * 记录操作日志
     */
    public void record(Core.ActionType actionType,
                       Core.TargetType targetType,
                       Long targetId,
                       Core.LogResult result) {
        try {
            SystemLog log = new SystemLog();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null) {
                log.setUsername(auth.getPrincipal().toString());
            }
            Object uid = auth != null ? auth.getDetails() : null;
            if (uid instanceof Long) {
                log.setUserId((Long) uid);
            }
            log.setActionType(actionType.name());
            log.setTargetType(targetType.name());
            log.setTargetId(targetId);
            log.setResult(result == null ? Core.LogResult.SUCCESS.name() : result.name());
            log.setIp(getClientIp());
            systemLogMapper.insert(log);
        } catch (Exception e) {
            log.error("记录系统日志失败", e);
        }
    }

    /**
     * 记录操作日志（无操作对象ID）
     */
    public void record(Core.ActionType actionType,
                       Core.TargetType targetType,
                       Core.LogResult result) {
        record(actionType, targetType, null, result);
    }

    /**
     * 分页查询日志（支持按用户名、操作类型、操作对象、结果筛选）
     */
    public PageResult<SystemLog> listLogsPage(String username,
                                              String actionType,
                                              String targetType,
                                              String result,
                                              int page,
                                              int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<SystemLog> list = systemLogMapper.searchLogs(username, actionType, targetType, result, offset, size);
        long total = systemLogMapper.countLogs(username, actionType, targetType, result);
        return new PageResult<>(list, total, page, size);
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp() {
        if (request == null) return null;
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty()) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    public boolean clearLog() {
        systemLogMapper.deleteAll();
        return true;
    }
}
