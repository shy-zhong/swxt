package com.swxt.manager.controller;

import com.swxt.manager.config.Core;
import com.swxt.manager.dto.Result;
import com.swxt.manager.service.FileService;
import com.swxt.manager.service.SystemLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器（仅管理员）
 */
@RestController
public class FileController {

    private final FileService fileService;
    private final SystemLogService logService;

    public FileController(FileService fileService, SystemLogService logService) {
        this.fileService = fileService;
        this.logService = logService;
    }

    /**
     * 上传图片：UUID 重命名后保存本地，返回可访问的 URL 路径
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        String url = fileService.store(file);
        logService.record(Core.ActionType.CREATE, Core.TargetType.PRODUCT, null, Core.LogResult.SUCCESS);
        return Result.success(url);
    }
}
