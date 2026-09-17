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
        // 最近3天全天的秒级时间戳 (北京时间)
        long beginTs = today.minusDays(3).atStartOfDay(ZoneId.of("Asia/Shanghai")).toEpochSecond();
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
        // 测试 v2 接口
        String v2BodyJson = String.format("{\"email\":\"%s\",\"dis_app_id\":2000019,\"page\":1,\"page_size\":50}", email);
        Map<String, String> v2Params = new HashMap<>();
        v2Params.put("company_id", COMPANY_ID);
        v2Params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        v2Params.put("nonce", UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        String v2Sign = FlicknovelSignUtil.generateSign(v2Params, v2BodyJson, PRIVATE_KEY);
        v2Params.put("sign", v2Sign);
        StringBuilder v2UrlBuilder = new StringBuilder(BASE_URL).append("/open/recharge_template/query/v2?");
        boolean v2F = true;
        for (Map.Entry<String, String> e : v2Params.entrySet()) {
            if (!v2F) v2UrlBuilder.append("&");
            v2UrlBuilder.append(URLEncoder.encode(e.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(e.getValue(), "UTF-8"));
            v2F = false;
        }
        try {
            URL u = new URL(v2UrlBuilder.toString());
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json");
            c.setDoOutput(true);
            try (OutputStream os = c.getOutputStream()) { os.write(v2BodyJson.getBytes(StandardCharsets.UTF_8)); }
            int code = c.getResponseCode();
            BufferedReader r = new BufferedReader(new InputStreamReader(code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = r.readLine()) != null) sb.append(l);
            r.close();
            java.nio.file.Files.write(java.nio.file.Paths.get("target/v2_resp.json"), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {}

        // 测试 v1 接口
        String v1BodyJson = String.format("{\"email\":\"%s\",\"dis_app_id\":2000019,\"page\":1,\"page_size\":50}", email);
        Map<String, String> v1Params = new HashMap<>();
        v1Params.put("company_id", COMPANY_ID);
        v1Params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        v1Params.put("nonce", UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        String v1Sign = FlicknovelSignUtil.generateSign(v1Params, v1BodyJson, PRIVATE_KEY);
        v1Params.put("sign", v1Sign);
        StringBuilder v1UrlBuilder = new StringBuilder(BASE_URL).append("/open/recharge_template/query/v1?");
        boolean v1F = true;
        for (Map.Entry<String, String> e : v1Params.entrySet()) {
            if (!v1F) v1UrlBuilder.append("&");
            v1UrlBuilder.append(URLEncoder.encode(e.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(e.getValue(), "UTF-8"));
            v1F = false;
        }
        try {
            URL u = new URL(v1UrlBuilder.toString());
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json");
            c.setDoOutput(true);
            try (OutputStream os = c.getOutputStream()) { os.write(v1BodyJson.getBytes(StandardCharsets.UTF_8)); }
            int code = c.getResponseCode();
            BufferedReader r = new BufferedReader(new InputStreamReader(code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = r.readLine()) != null) sb.append(l);
            r.close();
            java.nio.file.Files.write(java.nio.file.Paths.get("target/v1_resp.json"), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {}

        // 同时测试推广链接
        String promoBodyJson = String.format("{\"email\":\"%s\",\"page\":1,\"page_size\":50}", email);
        Map<String, String> pParams = new HashMap<>();
        pParams.put("company_id", COMPANY_ID);
        pParams.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        pParams.put("nonce", UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        String pSign = FlicknovelSignUtil.generateSign(pParams, promoBodyJson, PRIVATE_KEY);
        pParams.put("sign", pSign);

        StringBuilder pUrlBuilder = new StringBuilder(BASE_URL).append("/open/promotion/query/v1?");
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
            os.write(promoBodyJson.getBytes(StandardCharsets.UTF_8));
        }
        int pRespCode = pConn.getResponseCode();
        BufferedReader pReader = new BufferedReader(new InputStreamReader(
                pRespCode >= 200 && pRespCode < 300 ? pConn.getInputStream() : pConn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder pRespBody = new StringBuilder();
        String pLine;
        while ((pLine = pReader.readLine()) != null) pRespBody.append(pLine).append("\n");
        pReader.close();

        System.out.println("【Promotion Query Response】HTTP " + pRespCode);
        System.out.println(pRespBody.toString());
        java.nio.file.Files.write(java.nio.file.Paths.get("target/promo_resp.json"), pRespBody.toString().getBytes(StandardCharsets.UTF_8));
    }
}
