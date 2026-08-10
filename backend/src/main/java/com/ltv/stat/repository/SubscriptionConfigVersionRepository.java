package com.ltv.stat.repository;

import com.ltv.stat.entity.SubscriptionConfigVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionConfigVersionRepository extends JpaRepository<SubscriptionConfigVersion, Long> {

    Optional<SubscriptionConfigVersion> findTopByLandingPageIdAndProductIdOrderByVersionNumDesc(String landingPageId, String productId);

    @Query("SELECT v FROM SubscriptionConfigVersion v WHERE v.landingPageId = :pId " +
           "AND v.firstPriceCent = :priceCent " +
           "AND :targetTime >= v.effectiveStartTime " +
           "AND (v.effectiveEndTime IS NULL OR :targetTime <= v.effectiveEndTime) " +
           "ORDER BY v.versionNum DESC")
    List<SubscriptionConfigVersion> findMatchingFirstPriceVersions(@Param("pId") String pId,
                                                                    @Param("priceCent") Integer priceCent,
                                                                    @Param("targetTime") LocalDateTime targetTime);

    @Query("SELECT v FROM SubscriptionConfigVersion v WHERE v.landingPageId = :pId " +
           "AND v.renewPriceCent = :priceCent " +
           "AND :targetTime >= v.effectiveStartTime " +
           "AND (v.effectiveEndTime IS NULL OR :targetTime <= v.effectiveEndTime) " +
           "ORDER BY v.versionNum DESC")
    List<SubscriptionConfigVersion> findMatchingRenewPriceVersions(@Param("pId") String pId,
                                                                    @Param("priceCent") Integer priceCent,
                                                                    @Param("targetTime") LocalDateTime targetTime);

    @Query("SELECT v FROM SubscriptionConfigVersion v WHERE v.landingPageId = :pId " +
           "AND :targetTime >= v.effectiveStartTime " +
           "AND (v.effectiveEndTime IS NULL OR :targetTime <= v.effectiveEndTime) " +
           "ORDER BY v.versionNum DESC")
    List<SubscriptionConfigVersion> findMatchingPageVersions(@Param("pId") String pId,
                                                              @Param("targetTime") LocalDateTime targetTime);

    @Query("SELECT v FROM SubscriptionConfigVersion v WHERE v.subPeriodDays = :period " +
           "AND :targetTime >= v.effectiveStartTime " +
           "AND (v.effectiveEndTime IS NULL OR :targetTime <= v.effectiveEndTime) " +
           "ORDER BY v.versionNum DESC")
    List<SubscriptionConfigVersion> findMatchingPeriodVersions(@Param("period") Integer period,
                                                                @Param("targetTime") LocalDateTime targetTime);
}
