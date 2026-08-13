package com.ltv.stat.controller;

import com.ltv.stat.dto.*;
import com.ltv.stat.entity.DailyRechargeDistribution;
import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.entity.LtvLaunchConfig;
import com.ltv.stat.entity.LtvPredictBenchmark;
import com.ltv.stat.service.DailyRechargeStatService;
import com.ltv.stat.service.LtvBenchmarkService;
import com.ltv.stat.service.LtvPredictService;
import com.ltv.stat.service.LtvStatService;
import com.ltv.stat.service.OrderSyncService;
import com.ltv.stat.service.SubscribeConfigSyncService;
import com.ltv.stat.service.UserService;
import com.ltv.stat.util.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/ltv")
public class LtvController {

    private final LtvStatService ltvStatService;
    private final DailyRechargeStatService dailyRechargeStatService;
    private final OrderSyncService orderSyncService;
    private final LtvBenchmarkService ltvBenchmarkService;
    private final SubscribeConfigSyncService subscribeConfigSyncService;
    private final UserService userService;

    public LtvController(LtvStatService ltvStatService,
                         DailyRechargeStatService dailyRechargeStatService,
                         OrderSyncService orderSyncService,
                         LtvBenchmarkService ltvBenchmarkService,
                         SubscribeConfigSyncService subscribeConfigSyncService,
                         UserService userService) {
        this.ltvStatService = ltvStatService;
        this.dailyRechargeStatService = dailyRechargeStatService;
        this.orderSyncService = orderSyncService;
        this.ltvBenchmarkService = ltvBenchmarkService;
        this.subscribeConfigSyncService = subscribeConfigSyncService;
        this.userService = userService;
    }

    private Long resolveTargetUserId(Long targetUserId) {
        TokenInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            return targetUserId != null ? targetUserId : 1L;
        }
        if (targetUserId == null || targetUserId.equals(currentUser.getUserId())) {
            return currentUser.getUserId();
        }
        if (userService.canUserViewTarget(currentUser, targetUserId)) {
            return targetUserId;
        }
        return currentUser.getUserId();
    }

    private LocalDate parseFlexDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        String[] parts = dateStr.trim().split("[/\\-]");
        if (parts.length == 3) {
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            return LocalDate.of(year, month, day);
        }
        return LocalDate.parse(dateStr.trim().replace('/', '-'));
    }

    /**
     * 进入系统首页全量获取按投放日期排序的 LTV 统计表 (支持 targetUserId)
     */
    @GetMapping("/list")
    public ResponseEntity<LtvListResponseDto> getLtvList(@RequestParam(value = "targetUserId", required = false) Long targetUserId) {
        Long userId = resolveTargetUserId(targetUserId);
        LtvListResponseDto response = ltvStatService.getLtvListResponse(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 进入【每日充值分布】页面获取按自然日（支付日期）排序的充值分布数据 (支持 targetUserId)
     */
    @GetMapping("/daily-distribution")
    public ResponseEntity<DailyDistributionResponseDto> getDailyDistribution(@RequestParam(value = "targetUserId", required = false) Long targetUserId) {
        Long userId = resolveTargetUserId(targetUserId);
        List<DailyRechargeDistribution> list = dailyRechargeStatService.getDailyDistributionStats(userId);
        if (list.isEmpty()) {
            dailyRechargeStatService.calculateDailyDistributionStatsForUser(userId);
            list = dailyRechargeStatService.getDailyDistributionStats(userId);
        }
        DailyDistributionSummaryDto summary = dailyRechargeStatService.getDailyDistributionSummary(userId);
        DailyDistributionResponseDto response = new DailyDistributionResponseDto();
        response.setCode(0);
        response.setMsg("success");
        response.setData(list);
        response.setSummary(summary);
        response.setTotal(list.size());
        response.setUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 进入【平台汇总】页面获取全量平台订单（不区分落地页）的充值分布数据 (仅管理员/超级管理员可访问)
     */
    @GetMapping("/global-daily-distribution")
    public ResponseEntity<?> getGlobalDailyDistribution() {
        TokenInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权访问，仅管理员可查看全量平台汇总"));
        }
        List<DailyRechargeDistribution> list = dailyRechargeStatService.getGlobalDailyDistributionStats();
        DailyDistributionSummaryDto summary = dailyRechargeStatService.getGlobalDailyDistributionSummary();
        DailyDistributionResponseDto response = new DailyDistributionResponseDto();
        response.setCode(0);
        response.setMsg("success");
        response.setData(list);
        response.setSummary(summary);
        response.setTotal(list.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 重新计算 LTV 统计表与每日充值分布
     */
    @PostMapping("/recalculate")
    public ResponseEntity<LtvListResponseDto> recalculate(@RequestParam(value = "targetUserId", required = false) Long targetUserId) {
        Long userId = resolveTargetUserId(targetUserId);
        ltvStatService.calculateLtvStatsForUser(userId);
        dailyRechargeStatService.calculateDailyDistributionStatsForUser(userId);
        LtvListResponseDto response = ltvStatService.getLtvListResponse(userId);
        response.setMsg("重算 LTV 完成");
        return ResponseEntity.ok(response);
    }

    /**
     * 仅重新计算 LTV 统计表
     */
    @PostMapping("/recalculate-ltv")
    public ResponseEntity<LtvListResponseDto> recalculateLtvOnly(@RequestParam(value = "targetUserId", required = false) Long targetUserId) {
        Long userId = resolveTargetUserId(targetUserId);
        ltvStatService.calculateLtvStatsForUser(userId);
        LtvListResponseDto response = ltvStatService.getLtvListResponse(userId);
        response.setMsg("重算 LTV 报表完成！");
        return ResponseEntity.ok(response);
    }

    /**
     * 仅重新计算每日充值分布统计表
     */
    @PostMapping("/recalculate-daily-distribution")
    public ResponseEntity<DailyDistributionResponseDto> recalculateDailyDistributionOnly(@RequestParam(value = "targetUserId", required = false) Long targetUserId) {
        Long userId = resolveTargetUserId(targetUserId);
        dailyRechargeStatService.calculateDailyDistributionStatsForUser(userId);
        List<DailyRechargeDistribution> list = dailyRechargeStatService.getDailyDistributionStats(userId);
        DailyDistributionSummaryDto summary = dailyRechargeStatService.getDailyDistributionSummary(userId);
        DailyDistributionResponseDto response = new DailyDistributionResponseDto();
        response.setCode(0);
        response.setMsg("重算每日充值分析完成！");
        response.setData(list);
        response.setSummary(summary);
        response.setTotal(list.size());
        response.setUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 仅同步抓取远程 API 订单（保存到 raw_order 表，不触发统计重算）
     */
    @PostMapping("/sync-orders")
    public ResponseEntity<Map<String, Object>> syncOrdersOnly(@RequestBody(required = false) Map<String, String> body) {
        LocalDate todayBj = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        String defaultEndTimeStr = todayBj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String startTimeStr = "2026-07-10";
        String endTimeStr = defaultEndTimeStr;

        if (body != null) {
            if (body.get("startTime") != null && !body.get("startTime").trim().isEmpty()) {
                startTimeStr = body.get("startTime").trim();
            }
            if (body.get("endTime") != null && !body.get("endTime").trim().isEmpty()) {
                endTimeStr = body.get("endTime").trim();
            }
        }

        int totalSyncedOrders = 0;
        try {
            totalSyncedOrders = orderSyncService.syncOrdersAll(startTimeStr, endTimeStr);
        } catch (RuntimeException re) {
            if (re.getMessage() != null && re.getMessage().contains("TOKEN_EXPIRED")) {
                Map<String, Object> errResponse = new HashMap<>();
                errResponse.put("code", 4002);
                errResponse.put("msg", "订单接口 Authorization 登录 Token 已过期，请点击顶部【API Token 设置】更新 Token");
                return ResponseEntity.ok(errResponse);
            }
            throw re;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "订单同步完成(" + startTimeStr + " ~ " + endTimeStr + ")，共抓取/更新 " + totalSyncedOrders + " 笔订单！");
        response.put("totalSyncedOrders", totalSyncedOrders);
        return ResponseEntity.ok(response);
    }

    /**
     * 测试拉取指定日期（默认今日）订单（不传 landingPageId）
     */
    @PostMapping("/test-fetch-today-orders")
    public ResponseEntity<?> testFetchTodayOrders(@RequestBody(required = false) Map<String, String> body) {
        String dateStr = (body != null && body.containsKey("date") && !body.get("date").trim().isEmpty())
                ? body.get("date").trim()
                : LocalDate.now().toString();
        Map<String, Object> result = orderSyncService.testFetchTodayOrdersNoPid(dateStr);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户录入/修改某一投放日期的账户消耗和备注 (按用户隔离)
     */
    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> updateLaunchConfig(@RequestBody Map<String, Object> body) {
        String launchDateStr = (String) body.get("launchDate");
        Object spendObj = body.get("spend");
        String remark = (String) body.get("remark");

        Long targetUserId = null;
        if (body.containsKey("targetUserId") && body.get("targetUserId") != null) {
            try {
                targetUserId = Long.valueOf(body.get("targetUserId").toString());
            } catch (Exception ignored) {}
        }
        TokenInfo currentUser = UserContext.getCurrentUser();
        Long userId = (targetUserId != null) ? targetUserId : (currentUser != null ? currentUser.getUserId() : 1L);
        if (!userService.canUserModifyTarget(currentUser, userId)) {
            Map<String, Object> res = new HashMap<>();
            res.put("code", 403);
            res.put("msg", "该账户视图为只读模式，无法修改消耗或备注");
            return ResponseEntity.status(403).body(res);
        }

        if (launchDateStr == null || launchDateStr.trim().isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            res.put("code", 400);
            res.put("msg", "投放日期不能为空");
            return ResponseEntity.badRequest().body(res);
        }

        LocalDate launchDate = parseFlexDate(launchDateStr);

        BigDecimal spend = BigDecimal.ZERO;
        if (spendObj != null) {
            try {
                spend = new BigDecimal(spendObj.toString().trim());
            } catch (Exception ignored) {}
        }

        LtvLaunchConfig config = ltvStatService.saveLaunchConfig(userId, launchDate, spend, remark);

        // 保存消耗配置后重新触发当前用户的 LTV 指标重算
        ltvStatService.calculateLtvStatsForUser(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "消耗与备注更新成功！");
        response.put("data", config);
        return ResponseEntity.ok(response);
    }

    /**
     * 批量导入账户消耗与备注 (按用户隔离)
     */
    @PostMapping("/batch-spend")
    public ResponseEntity<Map<String, Object>> batchSpend(@RequestBody Map<String, Object> body) {
        Long targetUserId = null;
        if (body.containsKey("targetUserId") && body.get("targetUserId") != null) {
            try {
                targetUserId = Long.valueOf(body.get("targetUserId").toString());
            } catch (Exception ignored) {}
        }
        TokenInfo currentUser = UserContext.getCurrentUser();
        Long userId = (targetUserId != null) ? targetUserId : (currentUser != null ? currentUser.getUserId() : 1L);
        if (!userService.canUserModifyTarget(currentUser, userId)) {
            Map<String, Object> res = new HashMap<>();
            res.put("code", 403);
            res.put("msg", "该账户视图为只读模式，无法批量导入消耗");
            return ResponseEntity.status(403).body(res);
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items == null || items.isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            res.put("code", 400);
            res.put("msg", "导入数据不能为空");
            return ResponseEntity.badRequest().body(res);
        }

        int count = ltvStatService.batchSaveLaunchConfig(userId, items);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "批量导入账户消耗成功！");
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * 手动触发同步订单与全量统计，支持自定义时间范围 [startTime, endTime]
     */
    @PostMapping("/sync-and-calc")
    public ResponseEntity<Map<String, Object>> syncAndCalc(@RequestBody(required = false) Map<String, String> body) {
        LocalDate todayBj = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        String defaultEndTimeStr = todayBj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String startTimeStr = "2026-07-10";
        String endTimeStr = defaultEndTimeStr;

        if (body != null) {
            if (body.get("startTime") != null && !body.get("startTime").trim().isEmpty()) {
                startTimeStr = body.get("startTime").trim();
            }
            if (body.get("endTime") != null && !body.get("endTime").trim().isEmpty()) {
                endTimeStr = body.get("endTime").trim();
            }
        }

        int totalSyncedOrders = 0;
        try {
            totalSyncedOrders = orderSyncService.syncOrdersAll(startTimeStr, endTimeStr);
            ltvStatService.calculateAllLtvStats();
        } catch (RuntimeException re) {
            if (re.getMessage() != null && re.getMessage().contains("TOKEN_EXPIRED")) {
                Map<String, Object> errResponse = new HashMap<>();
                errResponse.put("code", 4002);
                errResponse.put("msg", "订单接口 Authorization 登录 Token 已过期，请点击顶部【API Token 设置】更新 Token");
                return ResponseEntity.ok(errResponse);
            }
            throw re;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "数据同步(" + startTimeStr + " ~ " + endTimeStr + ")与重新计算完成");
        response.put("totalSyncedOrders", totalSyncedOrders);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取 LTV 预测基准数据曲线
     */
    @GetMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> getBenchmark(
            @RequestParam(value = "dimensionType", defaultValue = "ALL") String dimensionType,
            @RequestParam(value = "dimensionValue", defaultValue = "DEFAULT") String dimensionValue,
            @RequestParam(value = "subPeriodDays", defaultValue = "1") Integer subPeriodDays) {
        List<LtvPredictBenchmark> benchmarkCurve = ltvBenchmarkService.getBenchmarkCurve(dimensionType, dimensionValue, subPeriodDays);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", benchmarkCurve);
        response.put("total", benchmarkCurve.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 手动重算预测基准库
     */
    @PostMapping("/recalculate-benchmark")
    public ResponseEntity<Map<String, Object>> recalculateBenchmark() {
        ltvBenchmarkService.recalculateAllBenchmarks();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "LTV 预测基准库重新计算完成");
        return ResponseEntity.ok(response);
    }

    /**
     * 手动触发拉取落地页配置与订阅产品明细版本库
     */
    @PostMapping("/sync-subscribe-configs")
    public ResponseEntity<Map<String, Object>> syncSubscribeConfigs() {
        int count = subscribeConfigSyncService.syncAllSubscribeConfigs();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "落地页与订阅配置版本数据同步完成，更新/保存 " + count + " 条版本快照！");
        response.put("savedVersionCount", count);
        return ResponseEntity.ok(response);
    }
}
