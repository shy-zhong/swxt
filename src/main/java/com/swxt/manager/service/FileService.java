package com.swxt.manager.service;

import com.swxt.manager.config.BusinessException;
import com.swxt.manager.config.Core;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 文件存储服务：上传重命名保存本地、按 URL 删除本地文件
 */
@Slf4j
@Service
public class FileService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    /**
     * 保存上传文件：UUID 重命名，写入 uploadDir/product/，返回相对 URL 路径
     */
    public String store(MultipartFile file) {
        return store(file, "product");
    }

    /**
     * 保存上传文件到指定子目录：UUID 重命名，返回相对 URL 路径
     */
    public String store(MultipartFile file, String subdir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST);
        }

        String originalName = file.getOriginalFilename();
        String ext = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST);
        }

        String fileName = UUID.randomUUID() + "." + ext;
        Path targetDir = Paths.get(uploadDir, subdir);
        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(fileName);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException(Core.ResultCode.SERVER_ERROR);
        }

        return "/uploads/" + subdir + "/" + fileName;
    }

    /**
     * 按 URL 删除本地文件，空值或删除失败安全跳过
     */
    public void deleteByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        if (!imageUrl.startsWith("/uploads/")) return;

        try {
            String relative = imageUrl.substring("/uploads/".length());
            Path target = Paths.get(uploadDir, relative);
            Files.deleteIfExists(target);
        } catch (Exception e) {
            log.warn("删除本地图片失败: {}", imageUrl, e);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1) : "";
    }
}
