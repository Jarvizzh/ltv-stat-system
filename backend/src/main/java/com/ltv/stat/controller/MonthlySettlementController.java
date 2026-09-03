package com.ltv.stat.controller;

import com.ltv.stat.dto.ApiResponseDto;
import com.ltv.stat.dto.MonthlySettlementItemDto;
import com.ltv.stat.dto.MonthlySettlementSaveRequestDto;
import com.ltv.stat.dto.TokenInfo;
import com.ltv.stat.dto.VisibleAccountDto;
import com.ltv.stat.entity.MonthlySettlementConfig;
import com.ltv.stat.service.MonthlySettlementService;
import com.ltv.stat.service.UserService;
import com.ltv.stat.util.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlement")
public class MonthlySettlementController {

    private final MonthlySettlementService settlementService;
    private final UserService userService;

    public MonthlySettlementController(MonthlySettlementService settlementService, UserService userService) {
        this.settlementService = settlementService;
        this.userService = userService;
    }

    private boolean checkPermission() {
        TokenInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.isSuperAdmin()) return true;
        return userService.hasPermSettlement(currentUser.getUserId());
    }

    @GetMapping("/list")
    public ResponseEntity<?> getMonthlySettlementList(
            @RequestParam(value = "settlementType", defaultValue = "PLATFORM_ALL") String settlementType,
            @RequestParam(value = "targetUserId", required = false) Long targetUserId
    ) {
        if (!checkPermission()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权访问，您未开通月份结算权限"));
        }

        TokenInfo currentUser = UserContext.getCurrentUser();
        if ("USER_ACCOUNT".equalsIgnoreCase(settlementType)) {
            Long effectiveUserId = targetUserId != null ? targetUserId : currentUser.getUserId();
            if (!userService.canUserViewTarget(currentUser, effectiveUserId)) {
                return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权查看该账号的结算数据"));
            }
            targetUserId = effectiveUserId;
        }

        List<MonthlySettlementItemDto> list = settlementService.getMonthlySettlementList(settlementType, targetUserId);
        return ResponseEntity.ok(ApiResponseDto.success(list));
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveSettlementConfig(@RequestBody MonthlySettlementSaveRequestDto body) {
        if (!checkPermission()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作，您未开通月份结算权限"));
        }

        TokenInfo currentUser = UserContext.getCurrentUser();
        if ("USER_ACCOUNT".equalsIgnoreCase(body.getSettlementType())) {
            Long targetUserId = body.getTargetUserId() != null ? body.getTargetUserId() : currentUser.getUserId();
            if (!userService.canUserModifyTarget(currentUser, targetUserId)) {
                return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权修改该账号的结算配置"));
            }
            body.setTargetUserId(targetUserId);
        }

        try {
            MonthlySettlementConfig saved = settlementService.saveSettlementConfig(body);
            return ResponseEntity.ok(ApiResponseDto.success("结算参数与配置保存成功！", saved));
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(400, ie.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDto.error(500, "保存失败: " + e.getMessage()));
        }
    }

    @GetMapping("/accounts")
    public ResponseEntity<?> getSettlementAccounts() {
        if (!checkPermission()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权访问，您未开通结算权限"));
        }
        TokenInfo currentUser = UserContext.getCurrentUser();
        List<VisibleAccountDto> accounts = userService.getSettlementAccountsForUser(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success(accounts));
    }
}
