package com.ltv.stat.service.flicknovel;

import com.ltv.stat.util.FlicknovelSignUtil;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrintOrderCurlTest {

    private static final String BASE_URL = "https://openapi.sinan-partner.com";
    private static final String COMPANY_ID = "355549587538358272";
    private static final String PRIVATE_KEY = "ymcPnTqpiQOAtROHJoeegoovJxS7wv6t0HLDUv5q3/G4qry6yKcvjYwhrBqwuEIMjfXMIIqDe0YUPu9JaPofMQ==";

    @Test
    public void generateTodayOrderCurlAndFetch() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        // 今天全天的秒级时间戳 (北京时间 00:00:00 到 23:59:59)
        long beginTs = today.atStartOfDay(ZoneId.of("Asia/Shanghai")).toEpochSecond();
        long endTs = today.plusDays(1).atStartOfDay(ZoneId.of("Asia/Shanghai")).toEpochSecond();

        String bodyJson = String.format("{\"begin_ts\":%d,\"end_ts\":%d,\"page\":1,\"page_size\":100}", beginTs, endTs);

        long nowSeconds = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("company_id", COMPANY_ID);
        queryParams.put("timestamp", String.valueOf(nowSeconds));
        queryParams.put("nonce", nonce);

        String sign = FlicknovelSignUtil.generateSign(queryParams, bodyJson, PRIVATE_KEY);
        queryParams.put("sign", sign);

        // 拼接 URL
        StringBuilder urlBuilder = new StringBuilder(BASE_URL).append("/get_order_list/v1?");
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (!first) urlBuilder.append("&");
            urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            first = false;
        }

        String fullUrl = urlBuilder.toString();

        String curlCmd = String.format("curl -X POST '%s' \\\n  -H 'Content-Type: application/json' \\\n  -d '%s'", fullUrl, bodyJson);

        System.out.println("================================================================================");
        System.out.println("【生成的 CURL 命令】");
        System.out.println(curlCmd);
        System.out.println("================================================================================");

        java.nio.file.Files.write(java.nio.file.Paths.get("target/curl_command.sh"), curlCmd.getBytes(StandardCharsets.UTF_8));

        // 实际发送请求获取返回
        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyJson.getBytes(StandardCharsets.UTF_8));
        }

        int respCode = conn.getResponseCode();
        System.out.println("HTTP 状态码: " + respCode);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                respCode >= 200 && respCode < 300 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder respBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            respBody.append(line).append("\n");
        }
        reader.close();

        java.nio.file.Files.write(java.nio.file.Paths.get("target/response.json"), respBody.toString().getBytes(StandardCharsets.UTF_8));

        System.out.println("【真实返回结果 JSON】");
        System.out.println(respBody.toString());
        System.out.println("================================================================================");
    }

    @Test
    public void testRechargeTemplateV2() throws Exception {
        String email = "charles_z0@163.com";
        String bodyJson = String.format("{\"email\":\"%s\",\"dis_app_id\":2000019,\"page\":1,\"page_size\":50}", email);
        long nowSeconds = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("company_id", COMPANY_ID);
        queryParams.put("timestamp", String.valueOf(nowSeconds));
        queryParams.put("nonce", nonce);

        String sign = FlicknovelSignUtil.generateSign(queryParams, bodyJson, PRIVATE_KEY);
        queryParams.put("sign", sign);

        StringBuilder urlBuilder = new StringBuilder(BASE_URL).append("/open/recharge_template/query/v2?");
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (!first) urlBuilder.append("&");
            urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            first = false;
        }

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyJson.getBytes(StandardCharsets.UTF_8));
        }

        int respCode = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                respCode >= 200 && respCode < 300 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder respBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) respBody.append(line).append("\n");
        reader.close();

        System.out.println("【Template V2 Query Response】HTTP " + respCode);
        System.out.println(respBody.toString());
        java.nio.file.Files.write(java.nio.file.Paths.get("target/v2_resp.json"), respBody.toString().getBytes(StandardCharsets.UTF_8));

        // 同时测试推广链接
        StringBuilder pUrlBuilder = new StringBuilder(BASE_URL).append("/open/promotion/query/v1?");
        String pSign = FlicknovelSignUtil.generateSign(queryParams, bodyJson, PRIVATE_KEY);
        Map<String, String> pParams = new HashMap<>(queryParams);
        pParams.put("sign", pSign);
        boolean pFirst = true;
        for (Map.Entry<String, String> entry : pParams.entrySet()) {
            if (!pFirst) pUrlBuilder.append("&");
            pUrlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            pFirst = false;
        }
        HttpURLConnection pConn = (HttpURLConnection) new URL(pUrlBuilder.toString()).openConnection();
        pConn.setRequestMethod("POST");
        pConn.setRequestProperty("Content-Type", "application/json");
        pConn.setDoOutput(true);
        try (OutputStream os = pConn.getOutputStream()) {
            os.write(bodyJson.getBytes(StandardCharsets.UTF_8));
        }
        int pRespCode = pConn.getResponseCode();
        BufferedReader pReader = new BufferedReader(new InputStreamReader(
                pRespCode >= 200 && pRespCode < 300 ? pConn.getInputStream() : pConn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder pRespBody = new StringBuilder();
        while ((line = pReader.readLine()) != null) pRespBody.append(line).append("\n");
        pReader.close();

        System.out.println("【Promotion Query Response】HTTP " + pRespCode);
        System.out.println(pRespBody.toString());
        java.nio.file.Files.write(java.nio.file.Paths.get("target/promo_resp.json"), pRespBody.toString().getBytes(StandardCharsets.UTF_8));
    }
}
