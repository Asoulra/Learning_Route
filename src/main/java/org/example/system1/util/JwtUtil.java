package org.example.system1.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtil {
    private static final String SECRET = "StudentGradeMgmtSystemSecretKey2024ForJWT!@#$%^&*";
    private static final Key KEY;
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24小时

    static {
        try {
            KEY = Keys.hmacShaKeyFor(SECRET.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("JWT密钥初始化失败", e);
        }
    }

    /**
     * 生成 Token
     */
    public static String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     */
    public static boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * 从 Token 获取用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("userId", Long.class);
        }
        return null;
    }

    /**
     * 从 Token 获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }

    /**
     * 从 Token 获取角色
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("role", String.class);
        }
        return null;
    }
}
