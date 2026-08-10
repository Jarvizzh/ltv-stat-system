package com.ltv.stat.repository;

import com.ltv.stat.entity.DailyRechargeDistribution;
import com.ltv.stat.entity.DailyRechargeDistributionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyRechargeDistributionRepository extends JpaRepository<DailyRechargeDistribution, DailyRechargeDistributionId> {

    Optional<DailyRechargeDistribution> findByUserIdAndDate(Long userId, LocalDate date);

    /**
     * 按用户和自然日（支付日期）倒序查询 2026-07-10 至今的每日充值分布统计表
     */
    List<DailyRechargeDistribution> findByUserIdAndDateGreaterThanEqualOrderByDateDesc(Long userId, LocalDate startDate);

    List<DailyRechargeDistribution> findByUserIdOrderByDateDesc(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DailyRechargeDistribution d WHERE d.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
