package com.ltv.stat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FlicknovelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String getAuthToken() {
        return "Bearer " + com.ltv.stat.util.TokenUtil.generateToken(1L, "superadmin", "SUPER_ADMIN", 7);
    }

    @Test
    public void testGetConfig() throws Exception {
        mockMvc.perform(get("/api/flicknovel/config")
                        .header("Authorization", getAuthToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.companyId").value("355549587538358272"))
                .andExpect(jsonPath("$.data.configured").value(true));
    }

    @Test
    public void testRawEndpointsNotExposed() throws Exception {
        // 验证 3 个原始接口不对外暴露（应返回 404）
        mockMvc.perform(post("/api/flicknovel/orders")
                        .header("Authorization", getAuthToken()))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/flicknovel/promotions")
                        .header("Authorization", getAuthToken()))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/flicknovel/recharge-templates")
                        .header("Authorization", getAuthToken()))
                .andExpect(status().isNotFound());
    }
}
