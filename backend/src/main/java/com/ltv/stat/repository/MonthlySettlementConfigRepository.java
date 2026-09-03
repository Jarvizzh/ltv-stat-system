package com.ltv.stat.repository;

import com.ltv.stat.entity.MonthlySettlementConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlySettlementConfigRepository extends JpaRepository<MonthlySettlementConfig, Long> {

    Optional<MonthlySettlementConfig> findBySettlementTypeAndTargetUserIdAndMonthStr(String settlementType, Long targetUserId, String monthStr);

    Optional<MonthlySettlementConfig> findBySettlementTypeAndTargetUserIdIsNullAndMonthStr(String settlementType, String monthStr);

    List<MonthlySettlementConfig> findBySettlementType(String settlementType);

    List<MonthlySettlementConfig> findBySettlementTypeAndTargetUserId(String settlementType, Long targetUserId);
}
