package com.ltv.stat.repository;

import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.entity.LtvDailyStatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LtvDailyStatRepository extends JpaRepository<LtvDailyStat, LtvDailyStatId> {
    Optional<LtvDailyStat> findByUserIdAndLaunchDate(Long userId, LocalDate launchDate);
    List<LtvDailyStat> findByUserIdAndLaunchDateGreaterThanEqualOrderByLaunchDateAsc(Long userId, LocalDate startDate);
    List<LtvDailyStat> findByUserIdOrderByLaunchDateAsc(Long userId);
    List<LtvDailyStat> findAllByOrderByLaunchDateAsc();

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM LtvDailyStat s WHERE s.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
