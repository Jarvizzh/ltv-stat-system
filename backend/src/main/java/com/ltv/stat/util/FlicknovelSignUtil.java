package com.ltv.stat.util;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 番茄海外 OpenAPI 签名工具类 (Ed25519 签名)
 * 规范：
 * 1. sign 以外的 QueryParam 按参数名升序排序拼接成 key=value&key=value
 * 2. 尾部追加 "&body=" + 请求体 JSON
 * 3. 用 ed25519 私钥加签该字符串
 * 4. 签名用 Base64 URLEncoding 编码
 */
public class FlicknovelSignUtil {

    private FlicknovelSignUtil() {}

    /**
     * 生成 Ed25519 签名
     *
     * @param queryParams query 参数 (不需要预先放 sign)
     * @param bodyJson 请求体 JSON 字符串
     * @param privateKeyBase64 Base64 编码的私钥 (32 字节或 64 字节)
     * @return Base64 URL 编码后的签名字符串
     */
    public static String generateSign(Map<String, String> queryParams, String bodyJson, String privateKeyBase64) {
        if (privateKeyBase64 == null || privateKeyBase64.trim().isEmpty()) {
            throw new IllegalArgumentException("Private key must not be empty");
        }

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64.trim());
        byte[] seed;
        if (privateKeyBytes.length == 32) {
            seed = privateKeyBytes;
        } else if (privateKeyBytes.length == 64) {
            seed = Arrays.copyOfRange(privateKeyBytes, 0, 32);
        } else {
            throw new IllegalArgumentException("Ed25519 private key length must be 32 or 64 bytes, got: " + privateKeyBytes.length);
        }

        String canonicalMessage = buildCanonicalMessage(queryParams, bodyJson);

        try {
            Ed25519PrivateKeyParameters privateKeyParams = new Ed25519PrivateKeyParameters(seed, 0);
            Ed25519Signer signer = new Ed25519Signer();
            signer.init(true, privateKeyParams);

            byte[] messageBytes = canonicalMessage.getBytes(StandardCharsets.UTF_8);
            signer.update(messageBytes, 0, messageBytes.length);
            byte[] signature = signer.generateSignature();

            return Base64.getUrlEncoder().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Ed25519 signature: " + e.getMessage(), e);
        }
    }

    /**
     * 构建待签名规范化消息
     */
    public static String buildCanonicalMessage(Map<String, String> queryParams, String bodyJson) {
        List<String> sortedKeys = new ArrayList<>();
        if (queryParams != null) {
            for (String key : queryParams.keySet()) {
                if (!"sign".equalsIgnoreCase(key)) {
                    sortedKeys.add(key);
                }
            }
        }
        Collections.sort(sortedKeys);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sortedKeys.size(); i++) {
            String key = sortedKeys.get(i);
            String val = queryParams.get(key);
            if (i > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(val != null ? val : "");
        }

        if (sb.length() > 0) {
            sb.append("&");
        }
        sb.append("body=").append(bodyJson != null ? bodyJson : "");

        return sb.toString();
    }
}
