package com.swxt.manager.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.qrcode.QrCodeException;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QrCodeService {
    @Autowired
    private QrConfig qrConfig;
    public void createCodeToFile(String content, String filePath) {
        try {
            QrCodeUtil.generate(content,qrConfig, FileUtil.file(filePath));
        } catch (QrCodeException e) {
            log.error("二维码生成到文件失败, content={}, path={}", content, filePath, e);
        }
    }
    public void createCodeToStream(String content, HttpServletResponse response) {
        try {
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-store");
            QrCodeUtil.generate(content,qrConfig, "png", response.getOutputStream());
        } catch (QrCodeException | IOException e) {
            log.error("二维码生成到输出流失败, content={}", content, e);
        }
    }
}
