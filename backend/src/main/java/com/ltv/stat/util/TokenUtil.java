package com.ltv.stat.util;

import com.ltv.stat.dto.TokenInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class TokenUtil {

    private static String secretKey = "zw-ltv-secret-auth-key-2026";

    @Value("${app.auth.secret-key:zw-ltv-secret-auth-key-2026}")
    public void setSecretKey(String key) {
        if (key != null && !key.trim().isEmpty()) {
            secretKey = key.trim();
        }
    }

    /**
     * 生成包含 3 天有效期的加密 Token
     * 结构: Base64(userId:username:role:expireTimestamp:signature)
     */
    public static String generateToken(Long userId, String username, String role, int expireDays) {
        long expireTime = System.currentTimeMillis() + (long) expireDays * 24 * 3600 * 1000;
        String rawPayload = userId + ":" + username + ":" + role + ":" + expireTime;
        String signature = sign(rawPayload);
        String fullToken = rawPayload + ":" + signature;
        return Base64.getUrlEncoder().encodeToString(fullToken.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 兼容旧版 generateToken
     */
    public static String generateToken(String username, int expireDays) {
        return generateToken(1L, username, "ADMIN", expireDays);
    }

    /**
     * 解析并验证 Token
     */
    public static TokenInfo parseToken(String tokenStr) {
        if (tokenStr == null || tokenStr.trim().isEmpty()) {
            return new TokenInfo(null, null, null, false);
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(tokenStr.trim()), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");

            // 支持新版 5 字段 Token: userId:username:role:expireTime:signature
            if (parts.length == 5) {
                Long userId = Long.parseLong(parts[0]);
                String username = parts[1];
                String role = parts[2];
                long expireTime = Long.parseLong(parts[3]);
                String signature = parts[4];

                if (System.currentTimeMillis() > expireTime) {
                    return new TokenInfo(userId, username, role, false);
                }

                String expectedSig = sign(userId + ":" + username + ":" + role + ":" + expireTime);
                if (expectedSig.equals(signature)) {
                    return new TokenInfo(userId, username, role, true);
                }
            }

            // 兼容旧版 3 字段 Token: username:expireTime:signature
            if (parts.length == 3) {
                String username = parts[0];
                long expireTime = Long.parseLong(parts[1]);
                String signature = parts[2];

                if (System.currentTimeMillis() > expireTime) {
                    return new TokenInfo(1L, username, "ADMIN", false);
                }

                String expectedSig = sign(username + ":" + expireTime);
                if (expectedSig.equals(signature)) {
                    return new TokenInfo(1L, username, "ADMIN", true);
                }
            }

        } catch (Exception e) {
            // parse error
        }

        return new TokenInfo(null, null, null, false);
    }

    /**
     * 验证 Token 是否合法且属于指定 username
     */
    public static boolean validateToken(String tokenStr, String expectedUsername) {
        TokenInfo info = parseToken(tokenStr);
        if (!info.isValid()) return false;
        return expectedUsername == null || expectedUsername.equals(info.getUsername());
    }

    private static String sign(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((payload + secretKey).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            return "sig-err";
        }
    }
}
