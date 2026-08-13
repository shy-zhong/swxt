package com.swxt.manager.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.qrcode.QrCodeException;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QrCodeService {
    @Autowired
    private QrConfig qrConfig;
    /**
     * 生成二维码并返回 base64 字符串
     */
    public String createCodeToBase64(String content) {
        try {
            BufferedImage image = QrCodeUtil.generate(content, qrConfig);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", out);
            return java.util.Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (QrCodeException | IOException e) {
            log.error("二维码生成 base64 失败, content={}", content, e);
            return null;
        }
    }
}
