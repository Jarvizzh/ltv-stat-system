package com.ltv.stat.repository;

import com.ltv.stat.entity.RawOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RawOrderRepository extends JpaRepository<RawOrder, Long> {
    Optional<RawOrder> findByOrderId(String orderId);
    List<RawOrder> findByRegisterDateEt(LocalDate registerDateEt);
    List<RawOrder> findByRegisterDateEtGreaterThanEqual(LocalDate startDate);
    Optional<RawOrder> findTopByMemberIdAndIsSubsAndRenewTypeOrderByIdAsc(String memberId, Integer isSubs, Integer renewType);
    List<RawOrder> findByLandingPageIdIn(List<String> landingPageIds);
}
