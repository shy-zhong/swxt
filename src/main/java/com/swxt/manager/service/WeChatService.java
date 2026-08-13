package com.swxt.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swxt.manager.Utils.JwtUtil;
import com.swxt.manager.config.Core;
import com.swxt.manager.entity.User;
import com.swxt.manager.mysql.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WeChatService {
    private final UserMapper userMapper;
    private final SystemConfigService systemConfigService;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";

    @Value("${wechat.appid:}")
    private String appId;

    @Value("${wechat.secret:}")
    private String secret;

    @Value("${wechat.redirect-base:}")
    private String redirectBase;

    private final Map<String, QrLoginState> qrSessions = new ConcurrentHashMap<>();

    private volatile String fixedSceneId;

    /**
     * 二维码登录会话状态
     */
    public record QrLoginState(boolean logged, String token, String username, String role, String authUrl) {
        static QrLoginState waiting(String authUrl) {
            return new QrLoginState(false, null, null, null, authUrl);
        }
        QrLoginState loggedIn(String token, String username, String role) {
            return new QrLoginState(true, token, username, role, authUrl);
        }
    }

    public WeChatService(UserMapper userMapper, SystemConfigService systemConfigService, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.systemConfigService = systemConfigService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 微信网页授权登录
     */
    public User loginByWeChat(String code) {
        if (appId.isEmpty() || secret.isEmpty()) {
            log.warn("微信登录未配置 appid/secret");
            return null;
        }
        String openid = fetchOpenid(code);
        if (openid == null) {
            return null;
        }
        User user = userMapper.loginByWechatOpenid(openid);
        if (user != null) {
            return user;
        }
        user = new User();
        user.setUsername("wx_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(Core.Role.USER);
        user.setStatus(1);
        user.setWechatOpenid(openid);
        if (userMapper.createNewUser(user) <= 0) {
            log.warn("失败, openid: {}", openid);
            return null;
        }
        log.info("成功: username={}, openid={}", user.getUsername(), openid);
        return user;
    }


    public Map<String, String> createQrLogin() {
        String sceneId = fixedSceneId;
        if (sceneId == null) {
            synchronized (this) {
                sceneId = fixedSceneId;
                if (sceneId == null) {
                    sceneId = UUID.randomUUID().toString().replace("-", "");
                    fixedSceneId = sceneId;
                }
            }
        }

        String redirectUri = redirectBase + "/api/login/wechat/qr/callback";
        String authUrl = "https://open.weixin.qq.com/connect/oauth2/authorize" +
                "?appid=" + appId +
                "&redirect_uri=" + java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=snsapi_base" +
                "&state=" + sceneId +
                "#wechat_redirect";
        qrSessions.put(sceneId, QrLoginState.waiting(authUrl));
        return Map.of("sceneId", sceneId, "authUrl", authUrl);
    }

    /**
     * 处理二维码回调
     */
    public boolean handleQrCallback(String sceneId, String code) {
        QrLoginState state = qrSessions.get(sceneId);
        if (state == null) {
            return false;
        }
        User user = loginByWeChat(code);
        if (user == null) {
            return false;
        }
        qrSessions.put(sceneId, state.loggedIn(jwtUtil.generateToken(user), user.getUsername(), user.getRole().name()));
        return true;
    }

    /**
     * PC 端取走登录结果
     */
    public QrLoginState consumeQrLogin(String sceneId) {
        QrLoginState state = qrSessions.get(sceneId);
        if (state == null || !state.logged()) {
            return state;
        }
        qrSessions.put(sceneId, QrLoginState.waiting(state.authUrl()));
        return state;
    }

    /**
     * 用授权 code 调用微信接口换取 openid
     */
    private String fetchOpenid(String code) {
        String url = String.format(TOKEN_URL, appId, secret, code);
        try {
            String body = restTemplate.getForObject(url, String.class);
            if (body == null || body.isEmpty()) {
                log.warn("微信接口返回为空: {}", url);
                return null;
            }
            Map<String, Object> resp = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            if (resp.get("errcode") != null) {
                log.warn("微信接口返回错误: errcode={}, errmsg={}", resp.get("errcode"), resp.get("errmsg"));
                return null;
            }
            Object openid = resp.get("openid");
            if (openid == null) {
                log.warn("微信响应缺少 openid: {}", body);
                return null;
            }
            return openid.toString();
        } catch (Exception e) {
            log.warn("微信 code2openid 异常", e);
            return null;
        }
    }
}
