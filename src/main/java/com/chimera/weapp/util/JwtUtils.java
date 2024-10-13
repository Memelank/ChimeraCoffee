package com.chimera.weapp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtils {

    private static final String SECRET_KEY = "yourSecretKey1145141919810chimera"; // 替换为你的密钥
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000 * 2; // 2 天
    public static final long REFRESH_TIME = 24 * 60 * 60 * 1000; // 1 天
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 生成 JWT
    public static String generateToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    // 解析 JWT
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 验证 JWT 是否过期
    public static boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }

}
