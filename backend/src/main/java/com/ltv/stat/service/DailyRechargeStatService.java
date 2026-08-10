package com.ltv.stat.service;

import com.ltv.stat.dto.DailyDistributionSummaryDto;
import com.ltv.stat.entity.DailyRechargeDistribution;
import com.ltv.stat.entity.RawOrder;
import com.ltv.stat.entity.SysUser;
import com.ltv.stat.repository.DailyRechargeDistributionRepository;
import com.ltv.stat.repository.RawOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.ltv.stat.service.LtvStatService.START_DATE;
import static com.ltv.stat.service.LtvStatService.getBjPayDate;
import static com.ltv.stat.service.LtvStatService.getBjRegisterDate;

/**
 * 每日充值统计与平台汇总服务类 (遵循单一职责原则 SRP)
 * 专职负责按“实际支付日期 (Pay Date)”维度的每日充值分布、新老客充值分布及全盘平台汇总计算。
 */
@Service
@Transactional
public class DailyRechargeStatService {

    private static final Logger log = LoggerFactory.getLogger(DailyRechargeStatService.class);

    private final RawOrderRepository rawOrderRepository;
    private final DailyRechargeDistributionRepository dailyRechargeDistributionRepository;
    private final UserService userService;
    private final LtvStatService ltvStatService;

    public DailyRechargeStatService(RawOrderRepository rawOrderRepository,
                                    DailyRechargeDistributionRepository dailyRechargeDistributionRepository,
                                    UserService userService,
                                    LtvStatService ltvStatService) {
        this.rawOrderRepository = rawOrderRepository;
        this.dailyRechargeDistributionRepository = dailyRechargeDistributionRepository;
        this.userService = userService;
        this.ltvStatService = ltvStatService;
    }

    public List<DailyRechargeDistribution> getDailyDistributionStats(Long userId) {
        if (userId == null) userId = 1L;
        List<String> userPIds = userService.getUserLandingPageIds(userId);
        if (userPIds == null || userPIds.isEmpty()) {
            dailyRechargeDistributionRepository.deleteByUserId(userId);
            dailyRechargeDistributionRepository.flush();
            return Collections.emptyList();
        }
        List<DailyRechargeDistribution> list = dailyRechargeDistributionRepository.findByUserIdAndDateGreaterThanEqualOrderByDateDesc(userId, START_DATE);
        if (list.isEmpty()) {
            calculateDailyDistributionStatsForUser(userId);
            list = dailyRechargeDistributionRepository.findByUserIdAndDateGreaterThanEqualOrderByDateDesc(userId, START_DATE);
        }
        return list;
    }

    public List<DailyRechargeDistribution> getDailyDistributionStats() {
        return getDailyDistributionStats(1L);
    }

    public List<DailyRechargeDistribution> getGlobalDailyDistributionStats() {
        LocalDate todayBj = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        List<RawOrder> allOrders = rawOrderRepository.findAll();

        Map<LocalDate, List<RawOrder>> ordersByPayDate = allOrders.stream()
                .filter(o -> {
                    LocalDate payDate = getBjPayDate(o);
                    return payDate != null && !payDate.isBefore(START_DATE);
                })
                .collect(Collectors.groupingBy(LtvStatService::getBjPayDate));

        List<DailyRechargeDistribution> statList = new ArrayList<>();
        LocalDate currDate = START_DATE;

        while (!currDate.isAfter(todayBj)) {
            List<RawOrder> dayOrders = ordersByPayDate.getOrDefault(currDate, Collections.emptyList());
            DailyRechargeDistribution stat = calculateSingleDayDistribution(0L, currDate, dayOrders);
            statList.add(stat);
            currDate = currDate.plusDays(1);
        }

        statList.sort(Comparator.comparing(DailyRechargeDistribution::getDate).reversed());
        return statList;
    }

    public DailyDistributionSummaryDto getGlobalDailyDistributionSummary() {
        List<RawOrder> orders = rawOrderRepository.findAll().stream()
                .filter(o -> {
                    LocalDate payDate = getBjPayDate(o);
                    return payDate != null && !payDate.isBefore(START_DATE);
                })
                .collect(Collectors.toList());
        return calculateDistributionSummaryFromOrders(orders);
    }

    public DailyDistributionSummaryDto getDailyDistributionSummary(Long userId) {
        if (userId == null) userId = 1L;
        List<RawOrder> orders = ltvStatService.getOrdersFilteredForUser(userId).stream()
                .filter(o -> {
                    LocalDate payDate = getBjPayDate(o);
                    return payDate != null && !payDate.isBefore(START_DATE);
                })
                .collect(Collectors.toList());
        return calculateDistributionSummaryFromOrders(orders);
    }

    public DailyDistributionSummaryDto getDailyDistributionSummary() {
        return getDailyDistributionSummary(1L);
    }

    public DailyDistributionSummaryDto calculateDistributionSummaryFromOrders(List<RawOrder> orders) {
        BigDecimal totalRecharge = orders.stream()
                .map(RawOrder::getOrderAmountUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RawOrder> newOrders = orders.stream().filter(o -> {
            LocalDate regDate = getBjRegisterDate(o);
            LocalDate payDate = getBjPayDate(o);
            return regDate != null && regDate.equals(payDate);
        }).collect(Collectors.toList());

        List<RawOrder> oldOrders = orders.stream().filter(o -> {
            LocalDate regDate = getBjRegisterDate(o);
            LocalDate payDate = getBjPayDate(o);
            return regDate != null && regDate.isBefore(payDate);
        }).collect(Collectors.toList());

        BigDecimal newRecharge = newOrders.stream().map(RawOrder::getOrderAmountUsd).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal oldRecharge = oldOrders.stream().map(RawOrder::getOrderAmountUsd).reduce(BigDecimal.ZERO, BigDecimal::add);

        Set<String> totalPaidUsersSet = orders.stream().map(RawOrder::getMemberId).collect(Collectors.toSet());
        Set<String> newPaidUsersSet = newOrders.stream().map(RawOrder::getMemberId).collect(Collectors.toSet());
        Set<String> oldPaidUsersSet = oldOrders.stream().map(RawOrder::getMemberId).collect(Collectors.toSet());

        int totalPaidUsers = totalPaidUsersSet.size();
        int newPaidUsers = newPaidUsersSet.size();
        int oldPaidUsers = oldPaidUsersSet.size();

        BigDecimal newArpu = newPaidUsers > 0 ? newRecharge.divide(BigDecimal.valueOf(newPaidUsers), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal oldArpu = oldPaidUsers > 0 ? oldRecharge.divide(BigDecimal.valueOf(oldPaidUsers), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal newRechargeRatio = totalRecharge.compareTo(BigDecimal.ZERO) > 0 ? newRecharge.divide(totalRecharge, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal oldRechargeRatio = totalRecharge.compareTo(BigDecimal.ZERO) > 0 ? oldRecharge.divide(totalRecharge, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Map<String, Long> userOrderCounts = orders.stream()
                .collect(Collectors.groupingBy(RawOrder::getMemberId, Collectors.counting()));

        long repeatPaidUsers = userOrderCounts.values().stream().filter(count -> count >= 2).count();
        BigDecimal repeatRate = totalPaidUsers > 0 ? BigDecimal.valueOf(repeatPaidUsers).divide(BigDecimal.valueOf(totalPaidUsers), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        LocalDate todayBj = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        String thisMonthStr = todayBj.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        String lastMonthStr = todayBj.minusMonths(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

        BigDecimal thisMonthRecharge = orders.stream()
                .filter(o -> {
                    LocalDate payDate = getBjPayDate(o);
                    return payDate != null && YearMonth.from(payDate).toString().equals(thisMonthStr);
                })
                .map(RawOrder::getOrderAmountUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal thisMonthRefund = orders.stream()
                .filter(o -> {
                    LocalDate payDate = getBjPayDate(o);
                    return payDate != null && YearMonth.from(payDate).toString().equals(thisMonthStr)
                            && o.getRefundStatus() != null && o.getRefundStatus() == 2;
                })
                .map(o -> o.getOrderAmountUsd() != null ? o.getOrderAmountUsd() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lastMonthRecharge = orders.stream()
                .filter(o -> {
                    LocalDate payDate = getBjPayDate(o);
                    return payDate != null && YearMonth.from(payDate).toString().equals(lastMonthStr);
                })
                .map(RawOrder::getOrderAmountUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lastMonthRefund = orders.stream()
                .filter(o -> {
                    LocalDate payDate = getBjPayDate(o);
                    return payDate != null && YearMonth.from(payDate).toString().equals(lastMonthStr)
                            && o.getRefundStatus() != null && o.getRefundStatus() == 2;
                })
                .map(o -> o.getOrderAmountUsd() != null ? o.getOrderAmountUsd() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DailyDistributionSummaryDto summary = new DailyDistributionSummaryDto();
        summary.setTotalRecharge(totalRecharge);
        summary.setNewRecharge(newRecharge);
        summary.setOldRecharge(oldRecharge);
        summary.setThisMonthRecharge(thisMonthRecharge);
        summary.setThisMonthRefund(thisMonthRefund);
        summary.setLastMonthRecharge(lastMonthRecharge);
        summary.setLastMonthRefund(lastMonthRefund);
        summary.setThisMonthStr(thisMonthStr);
        summary.setLastMonthStr(lastMonthStr);
        summary.setNewRechargeRatio(newRechargeRatio);
        summary.setOldRechargeRatio(oldRechargeRatio);
        summary.setTotalPaidUsers(totalPaidUsers);
        summary.setNewPaidUsers(newPaidUsers);
        summary.setOldPaidUsers(oldPaidUsers);
        summary.setNewArpu(newArpu);
        summary.setOldArpu(oldArpu);
        summary.setRepeatPaidUsers((int) repeatPaidUsers);
        summary.setRepeatRate(repeatRate);

        return summary;
    }

    @Transactional
    public void calculateDailyDistributionStatsForUser(Long userId) {
        if (userId == null) userId = 1L;
        LocalDate todayBj = LocalDate.now(ZoneId.of("Asia/Shanghai"));

        List<RawOrder> orders = ltvStatService.getOrdersFilteredForUser(userId);

        Map<LocalDate, List<RawOrder>> ordersByPayDate = orders.stream()
                .filter(o -> {
                    LocalDate payDate = getBjPayDate(o);
                    return payDate != null && !payDate.isBefore(START_DATE);
                })
                .collect(Collectors.groupingBy(LtvStatService::getBjPayDate));

        List<DailyRechargeDistribution> statList = new ArrayList<>();
        LocalDate currDate = START_DATE;

        while (!currDate.isAfter(todayBj)) {
            List<RawOrder> dayOrders = ordersByPayDate.getOrDefault(currDate, Collections.emptyList());
            DailyRechargeDistribution stat = calculateSingleDayDistribution(userId, currDate, dayOrders);
            statList.add(stat);
            currDate = currDate.plusDays(1);
        }

        dailyRechargeDistributionRepository.deleteByUserId(userId);
        dailyRechargeDistributionRepository.flush();
        dailyRechargeDistributionRepository.saveAll(statList);
        dailyRechargeDistributionRepository.flush();
    }

    @Transactional
    public void calculateAllDailyDistributionStats() {
        List<SysUser> users = userService.listAllUsers();
        if (users.isEmpty()) {
            calculateDailyDistributionStatsForUser(1L);
        } else {
            for (SysUser user : users) {
                calculateDailyDistributionStatsForUser(user.getId());
            }
        }
        log.info("Daily recharge distribution calculation completed for all active users.");
    }

    private DailyRechargeDistribution calculateSingleDayDistribution(Long userId, LocalDate payDate, List<RawOrder> dayOrders) {
        DailyRechargeDistribution stat = new DailyRechargeDistribution();
        stat.setUserId(userId);
        stat.setDate(payDate);

        if (dayOrders.isEmpty()) {
            stat.setTotalRecharge(BigDecimal.ZERO);
            stat.setSingleRecharge(BigDecimal.ZERO);
            stat.setSubsRecharge(BigDecimal.ZERO);
            stat.setTotalPaidUsers(0);
            stat.setSinglePaidUsers(0);
            stat.setSubsPaidUsers(0);

            stat.setNewRecharge(BigDecimal.ZERO);
            stat.setNewArpu(BigDecimal.ZERO);
            stat.setNewPaidUsers(0);
            stat.setNewSinglePaidUsers(0);
            stat.setNewSubsPaidUsers(0);

            stat.setOldRecharge(BigDecimal.ZERO);
            stat.setOldArpu(BigDecimal.ZERO);
            stat.setOldPaidUsers(0);
            stat.setOldSinglePaidUsers(0);
            stat.setOldSubsPaidUsers(0);

            stat.setRepeatPaidUsers(0);
            stat.setRepeatRate(BigDecimal.ZERO);
            return stat;
        }

        BigDecimal totalRecharge = dayOrders.stream().map(o -> o.getOrderAmountUsd() != null ? o.getOrderAmountUsd() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal singleRecharge = dayOrders.stream().filter(o -> o.getIsSubs() == null || o.getIsSubs() == 0).map(o -> o.getOrderAmountUsd() != null ? o.getOrderAmountUsd() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subsRecharge = dayOrders.stream().filter(o -> o.getIsSubs() != null && o.getIsSubs() == 1).map(o -> o.getOrderAmountUsd() != null ? o.getOrderAmountUsd() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        Set<String> totalPaidUserSet = dayOrders.stream().map(RawOrder::getMemberId).filter(id -> id != null && !id.trim().isEmpty()).collect(Collectors.toSet());
        Set<String> singlePaidUserSet = dayOrders.stream().filter(o -> o.getIsSubs() == null || o.getIsSubs() == 0).map(RawOrder::getMemberId).filter(id -> id != null && !id.trim().isEmpty()).collect(Collectors.toSet());
        Set<String> subsPaidUserSet = dayOrders.stream().filter(o -> o.getIsSubs() != null && o.getIsSubs() == 1).map(RawOrder::getMemberId).filter(id -> id != null && !id.trim().isEmpty()).collect(Collectors.toSet());

        List<RawOrder> newOrders = dayOrders.stream().filter(o -> {
            LocalDate regDate = getBjRegisterDate(o);
            return regDate != null && regDate.equals(payDate);
        }).collect(Collectors.toList());

        List<RawOrder> oldOrders = dayOrders.stream().filter(o -> {
            LocalDate regDate = getBjRegisterDate(o);
            return regDate != null && regDate.isBefore(payDate);
        }).collect(Collectors.toList());

        BigDecimal newRecharge = newOrders.stream().map(RawOrder::getOrderAmountUsd).reduce(BigDecimal.ZERO, BigDecimal::add);
        Set<String> newPaidUserSet = newOrders.stream().map(RawOrder::getMemberId).collect(Collectors.toSet());
        Set<String> newSinglePaidUserSet = newOrders.stream().filter(o -> o.getIsSubs() == null || o.getIsSubs() == 0).map(RawOrder::getMemberId).collect(Collectors.toSet());
        Set<String> newSubsPaidUserSet = newOrders.stream().filter(o -> o.getIsSubs() != null && o.getIsSubs() == 1).map(RawOrder::getMemberId).collect(Collectors.toSet());

        int newPaidCount = newPaidUserSet.size();
        BigDecimal newArpu = newPaidCount > 0 ? newRecharge.divide(BigDecimal.valueOf(newPaidCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal oldRecharge = oldOrders.stream().map(RawOrder::getOrderAmountUsd).reduce(BigDecimal.ZERO, BigDecimal::add);
        Set<String> oldPaidUserSet = oldOrders.stream().map(RawOrder::getMemberId).collect(Collectors.toSet());
        Set<String> oldSinglePaidUserSet = oldOrders.stream().filter(o -> o.getIsSubs() == null || o.getIsSubs() == 0).map(RawOrder::getMemberId).collect(Collectors.toSet());
        Set<String> oldSubsPaidUserSet = oldOrders.stream().filter(o -> o.getIsSubs() != null && o.getIsSubs() == 1).map(RawOrder::getMemberId).collect(Collectors.toSet());

        int oldPaidCount = oldPaidUserSet.size();
        BigDecimal oldArpu = oldPaidCount > 0 ? oldRecharge.divide(BigDecimal.valueOf(oldPaidCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal newRechargeRatio = totalRecharge.compareTo(BigDecimal.ZERO) > 0 ? newRecharge.divide(totalRecharge, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal oldRechargeRatio = totalRecharge.compareTo(BigDecimal.ZERO) > 0 ? oldRecharge.divide(totalRecharge, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Map<String, Long> dayUserOrderCounts = dayOrders.stream().collect(Collectors.groupingBy(RawOrder::getMemberId, Collectors.counting()));
        long dayRepeatPaidUsers = dayUserOrderCounts.values().stream().filter(c -> c >= 2).count();
        int totalPaidCount = totalPaidUserSet.size();
        BigDecimal repeatRate = totalPaidCount > 0 ? BigDecimal.valueOf(dayRepeatPaidUsers).divide(BigDecimal.valueOf(totalPaidCount), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        stat.setTotalRecharge(totalRecharge);
        stat.setSingleRecharge(singleRecharge);
        stat.setSubsRecharge(subsRecharge);
        stat.setTotalPaidUsers(totalPaidCount);
        stat.setSinglePaidUsers(singlePaidUserSet.size());
        stat.setSubsPaidUsers(subsPaidUserSet.size());

        stat.setNewRecharge(newRecharge);
        stat.setNewRechargeRatio(newRechargeRatio);
        stat.setNewArpu(newArpu);
        stat.setNewPaidUsers(newPaidCount);
        stat.setNewSinglePaidUsers(newSinglePaidUserSet.size());
        stat.setNewSubsPaidUsers(newSubsPaidUserSet.size());

        stat.setOldRecharge(oldRecharge);
        stat.setOldRechargeRatio(oldRechargeRatio);
        stat.setOldArpu(oldArpu);
        stat.setOldPaidUsers(oldPaidCount);
        stat.setOldSinglePaidUsers(oldSinglePaidUserSet.size());
        stat.setOldSubsPaidUsers(oldSubsPaidUserSet.size());

        stat.setRepeatPaidUsers((int) dayRepeatPaidUsers);
        stat.setRepeatRate(repeatRate);

        return stat;
    }
}
