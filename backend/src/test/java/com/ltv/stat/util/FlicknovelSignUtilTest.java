package com.ltv.stat.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FlicknovelSignUtilTest {

    @Test
    public void testSignGeneration() {
        String companyId = "355549587538358272";
        String privateKey = "ymcPnTqpiQOAtROHJoeegoovJxS7wv6t0HLDUv5q3/G4qry6yKcvjYwhrBqwuEIMjfXMIIqDe0YUPu9JaPofMQ==";

        Map<String, String> query = new HashMap<>();
        query.put("company_id", companyId);
        query.put("timestamp", "1726500000");
        query.put("nonce", "123456");

        String body = "{\"begin_ts\":1726400000,\"end_ts\":1726500000,\"page\":1,\"page_size\":100}";

        String sign = FlicknovelSignUtil.generateSign(query, body, privateKey);
        assertNotNull(sign);
        assertFalse(sign.isEmpty());
        System.out.println("Generated sign: " + sign);
    }
}
