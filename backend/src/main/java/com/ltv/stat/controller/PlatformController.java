package com.ltv.stat.controller;

import com.ltv.stat.dto.ApiResponseDto;
import com.ltv.stat.dto.TokenInfo;
import com.ltv.stat.entity.PlatformConfig;
import com.ltv.stat.entity.SysUser;
import com.ltv.stat.enums.PlatformEnum;
import com.ltv.stat.repository.PlatformConfigRepository;
import com.ltv.stat.service.UserService;
import com.ltv.stat.util.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/platform")
public class PlatformController {

    private final PlatformConfigRepository platformConfigRepository;
    private final UserService userService;

    public PlatformController(PlatformConfigRepository platformConfigRepository, UserService userService) {
        this.platformConfigRepository = platformConfigRepository;
        this.userService = userService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> listPlatforms() {
        TokenInfo currentUser = UserContext.getCurrentUser();
        SysUser user = null;
        if (currentUser != null && currentUser.getUserId() != null) {
            user = userService.findById(currentUser.getUserId()).orElse(null);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        // Always include ALL if permitted
        boolean canAccessAll = user == null || user.hasPlatformAccess("ALL");
        if (canAccessAll) {
            Map<String, Object> allItem = new LinkedHashMap<>();
            allItem.put("code", PlatformEnum.ALL.getCode());
            allItem.put("name", PlatformEnum.ALL.getDisplayName());
            allItem.put("enabled", true);
            allItem.put("launchStartDate", PlatformEnum.ALL.getLaunchStartDateStr());
            result.add(allItem);
        }

        // Fetch registered platforms from config repository
        List<PlatformConfig> configs = platformConfigRepository.findByStatusOrderByCreatedAtAsc(1);
        Set<String> addedCodes = new HashSet<>();

        if (configs != null && !configs.isEmpty()) {
            for (PlatformConfig config : configs) {
                if (user == null || user.hasPlatformAccess(config.getPlatformCode())) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("code", config.getPlatformCode());
                    item.put("name", config.getPlatformName());
                    item.put("enabled", true);
                    String startDateStr = config.getLaunchStartDate() != null
                            ? config.getLaunchStartDate().toString()
                            : PlatformEnum.fromCode(config.getPlatformCode()).map(PlatformEnum::getLaunchStartDateStr).orElse("2026-07-10");
                    item.put("launchStartDate", startDateStr);
                    result.add(item);
                    addedCodes.add(config.getPlatformCode().toLowerCase());
                }
            }
        }

        // Fallback to PlatformEnum actual platforms if not present in DB
        for (PlatformEnum p : PlatformEnum.getActualPlatforms()) {
            if (!addedCodes.contains(p.getCode().toLowerCase())) {
                if (user == null || user.hasPlatformAccess(p.getCode())) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("code", p.getCode());
                    item.put("name", p.getDisplayName());
                    item.put("enabled", true);
                    item.put("launchStartDate", p.getLaunchStartDateStr());
                    result.add(item);
                    addedCodes.add(p.getCode().toLowerCase());
                }
            }
        }

        return ResponseEntity.ok(ApiResponseDto.success(result));
    }
}
