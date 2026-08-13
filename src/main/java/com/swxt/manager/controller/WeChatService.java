package com.swxt.manager.controller;


import com.swxt.manager.Utils.JwtUtil;
import com.swxt.manager.mysql.UserMapper;
import com.swxt.manager.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeChatService {
    private final UserMapper userMapper;
    private final SystemConfigService systemConfigService;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    private static final String USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";

    public WeChatService(UserMapper userMapper, SystemConfigService systemConfigService, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.systemConfigService = systemConfigService;
        this.jwtUtil = jwtUtil;
    }
}
