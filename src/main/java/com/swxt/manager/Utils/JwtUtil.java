package com.swxt.manager.Utils;

import com.swxt.manager.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
public class JwtUtil {

    
    private final SecretKey key;

    
    private final long duration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long duration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.duration = duration;
    }

    /**
     * 为用户生成 JWT：主题为用户名，附带 userId 与角色声明
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + duration);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从令牌中取出用户ID
     */
    public Long getUserIdFromToken(Claims claims) {
        Object id = claims.get("userId");
        if (id == null) return null;
        if (id instanceof Number) return ((Number) id).longValue();
        try {
            return Long.parseLong(id.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析令牌并返回 Claims（校验签名与过期时间）
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 校验令牌是否合法（签名正确且未过期）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中取出用户名（主题）
     */
    public String getUsernameFromToken(String token) {

        return parseToken(token).getSubject();
    }
}
