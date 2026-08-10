package com.ltv.stat.repository;

import com.ltv.stat.entity.UserSubscriptionPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionPeriodRepository extends JpaRepository<UserSubscriptionPeriod, Long> {

    Optional<UserSubscriptionPeriod> findByMemberId(String memberId);

    List<UserSubscriptionPeriod> findByMemberIdIn(Collection<String> memberIds);
}
