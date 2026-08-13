package com.ltv.stat.controller;

import com.ltv.stat.dto.ApiResponseDto;
import com.ltv.stat.dto.LandingPageConfigItem;
import com.ltv.stat.dto.TokenInfo;
import com.ltv.stat.dto.UserLandingPageConfigResponseDto;
import com.ltv.stat.dto.UserLandingPageUpdateRequestDto;
import com.ltv.stat.dto.VisibleAccountDto;
import com.ltv.stat.service.DailyRechargeStatService;
import com.ltv.stat.service.LtvStatService;
import com.ltv.stat.service.UserService;
import com.ltv.stat.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserConfigController {

    private static final Logger log = LoggerFactory.getLogger(UserConfigController.class);

    private final UserService userService;
    private final LtvStatService ltvStatService;
    private final DailyRechargeStatService dailyRechargeStatService;

    public UserConfigController(UserService userService, LtvStatService ltvStatService, DailyRechargeStatService dailyRechargeStatService) {
        this.userService = userService;
        this.ltvStatService = ltvStatService;
        this.dailyRechargeStatService = dailyRechargeStatService;
    }

    @GetMapping("/visible-accounts")
    public ResponseEntity<?> getVisibleAccounts() {
        TokenInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            return ResponseEntity.status(401).body(ApiResponseDto.error(401, "未登录"));
        }
        List<VisibleAccountDto> visibleAccounts = userService.getVisibleAccountsForUser(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success(visibleAccounts));
    }

    @GetMapping("/landing-pages")
    public ResponseEntity<?> getMyLandingPages(@RequestParam(value = "targetUserId", required = false) Long targetUserId) {
        TokenInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            return ResponseEntity.status(401).body(ApiResponseDto.error(401, "未登录"));
        }

        Long userId = (targetUserId != null) ? targetUserId : currentUser.getUserId();
        if (!userService.canUserViewTarget(currentUser, userId)) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权访问该账户的视图"));
        }

        List<LandingPageConfigItem> configs = userService.getUserLandingPageConfigs(userId);
        List<String> pageIds = configs.stream().map(LandingPageConfigItem::getLandingPageId).collect(Collectors.toList());

        return ResponseEntity.ok(new UserLandingPageConfigResponseDto(configs, pageIds));
    }

    @PostMapping("/landing-pages")
    public ResponseEntity<?> updateMyLandingPages(@RequestBody UserLandingPageUpdateRequestDto body) {
        TokenInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            return ResponseEntity.status(401).body(ApiResponseDto.error(401, "未登录"));
        }

        Long userId = (body != null && body.getTargetUserId() != null) ? body.getTargetUserId() : currentUser.getUserId();
        if (!userService.canUserModifyTarget(currentUser, userId)) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "该账户视图为只读模式，无法修改落地页配置"));
        }

        try {
            if (body != null && body.getLandingPages() != null) {
                userService.updateUserLandingPageConfigs(userId, body.getLandingPages());
            } else if (body != null && body.getLandingPageIds() != null) {
                userService.updateUserLandingPageIds(userId, body.getLandingPageIds());
            }

            // 立即触发当前用户的报表重算
            ltvStatService.calculateLtvStatsForUser(userId);
            dailyRechargeStatService.calculateDailyDistributionStatsForUser(userId);

            return ResponseEntity.ok(ApiResponseDto.success("落地页配置已更新，并完成个人报表秒级重算！", null));
        } catch (Exception e) {
            log.error("Failed to update landing pages for user " + userId, e);
            return ResponseEntity.status(500).body(ApiResponseDto.error(500, "保存失败: " + e.getMessage()));
        }
    }
}
