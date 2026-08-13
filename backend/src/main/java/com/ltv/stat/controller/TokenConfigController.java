package com.ltv.stat.controller;

import com.ltv.stat.dto.ApiResponseDto;
import com.ltv.stat.dto.TokenConfigDto;
import com.ltv.stat.dto.TokenInfo;
import com.ltv.stat.service.OrderSyncService;
import com.ltv.stat.util.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
public class TokenConfigController {

    private final OrderSyncService orderSyncService;

    public TokenConfigController(OrderSyncService orderSyncService) {
        this.orderSyncService = orderSyncService;
    }

    private boolean checkSuperAdmin() {
        TokenInfo currentUser = UserContext.getCurrentUser();
        return currentUser != null && currentUser.isSuperAdmin();
    }

    @GetMapping("/get")
    public ResponseEntity<?> getToken() {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权访问，API 设置仅超级管理员可见"));
        }
        TokenConfigDto config = new TokenConfigDto(
                orderSyncService.getActiveAuthorization(),
                orderSyncService.getActiveCookie()
        );
        return ResponseEntity.ok(ApiResponseDto.success(config));
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateToken(@RequestBody TokenConfigDto body) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权访问，API 设置仅超级管理员可见"));
        }
        String authorization = body != null ? body.getAuthorization() : null;
        String cookie = body != null ? body.getCookie() : null;

        orderSyncService.updateApiToken(authorization, cookie);

        return ResponseEntity.ok(ApiResponseDto.success("Token 更新成功！", null));
    }
}
