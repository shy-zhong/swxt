package com.swxt.manager.config;

import com.swxt.manager.Utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

/**
 * 构造过滤器：注入 JWT 工具类
 */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

/**
 * 解析请求头 Authorization 中的 Bearer Token，校验通过后注入 Spring Security 认证上下文
 * principal=用户名；details=userId（供日志服务取用）
 */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);


            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.parseToken(token);
                String username = claims.getSubject();
                Long userId = jwtUtil.getUserIdFromToken(claims);
                String role = claims.get("role", String.class);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                username, null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                auth.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }


        filterChain.doFilter(request, response);
    }
}
