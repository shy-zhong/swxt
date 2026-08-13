package com.swxt.manager.controller;

import com.swxt.manager.dto.Result;
import com.swxt.manager.service.QrCodeService;
import com.swxt.manager.service.WeChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 自建二维码登录：PC 显示二维码，手机扫码后微信内完成授权，PC 轮询状态完成登录
 */
@RestController
@RequestMapping("/login/wechat/qr")
public class QrController {

    private final WeChatService weChatService;
    private final QrCodeService qrCodeService;

    public QrController(WeChatService weChatService, QrCodeService qrCodeService) {
        this.weChatService = weChatService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * 创建二维码登录会话
     */
    @PostMapping("/create")
    public Result<Map<String, String>> create() {
        Map<String, String> session = weChatService.createQrLogin();
        String qrBase64 = qrCodeService.createCodeToBase64(session.get("authUrl"));
        if (qrBase64 == null) {
            return Result.error("二维码生成失败");
        }
        Map<String, String> result = new HashMap<>();
        result.put("sceneId", session.get("sceneId"));
        result.put("qrBase64", qrBase64);
        return Result.success(result);
    }

    /**
     * 微信授权回调
     */
    @GetMapping("/callback")
    public void callback(@RequestParam("code") String code,
                         @RequestParam("state") String state,
                         HttpServletResponse response) throws IOException {
        boolean ok = weChatService.handleQrCallback(state, code);
        response.setContentType("text/html;charset=UTF-8");
        if (ok) {
            response.getWriter().write("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
                    "<body style='text-align:center;padding-top:80px;font-family:sans-serif;'>" +
                    "<h2>登录成功</h2><p>请返回电脑端查看登录结果。</p></body></html>");
        } else {
            response.getWriter().write("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
                    "<body style='text-align:center;padding-top:80px;font-family:sans-serif;'>" +
                    "<h2>登录失败或已过期</h2><p>请返回电脑端重新扫码。</p></body></html>");
        }
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> status(@RequestParam("sceneId") String sceneId) {
        WeChatService.QrLoginState state = weChatService.consumeQrLogin(sceneId);
        Map<String, Object> data = new HashMap<>();
        if (state == null) {
            data.put("status", "invalid");
            return Result.success(data);
        }
        if (state.logged()) {
            data.put("status", "success");
            data.put("token", state.token());
            data.put("username", state.username());
            data.put("role", state.role());
        } else {
            data.put("status", "waiting");
        }
        return Result.success(data);
    }
}
