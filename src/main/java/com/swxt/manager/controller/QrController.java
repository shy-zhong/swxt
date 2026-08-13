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
     * 创建二维码登录会话：返回 sceneId 与授权地址
     */
    @PostMapping("/create")
    public Result<Map<String, String>> create(@RequestParam("redirectBase") String redirectBase) {
        Map<String, String> result = weChatService.createQrLogin(redirectBase);
        return Result.success(result);
    }

    /**
     * 输出二维码图片：内容为授权地址
     */
    @GetMapping("/image")
    public void image(@RequestParam("sceneId") String sceneId,
                      @RequestParam(value = "authUrl", required = false) String authUrl,
                      HttpServletResponse response) throws IOException {
        if (authUrl == null || authUrl.isEmpty()) {
            Map<String, String> session = weChatService.getQrAuthUrl(sceneId);
            if (session == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "二维码会话不存在或已过期");
                return;
            }
            authUrl = session.get("authUrl");
        }
        qrCodeService.createCodeToStream(authUrl, response);
    }

    /**
     * 微信授权回调：手机端扫码后在微信内静默授权，跳回此地址完成登录
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

    /**
     * PC 端轮询登录状态：logged=true 时携带 token/username/role
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> status(@RequestParam("sceneId") String sceneId) {
        WeChatService.QrLoginState state = weChatService.getQrLoginState(sceneId);
        Map<String, Object> data = new HashMap<>();
        if (state == null) {
            data.put("status", "expired");
            return Result.success(data);
        }
        if (state.isLogged()) {
            data.put("status", "success");
            data.put("token", state.getToken());
            data.put("username", state.getUsername());
            data.put("role", state.getRole());
        } else {
            data.put("status", "waiting");
        }
        return Result.success(data);
    }
}
