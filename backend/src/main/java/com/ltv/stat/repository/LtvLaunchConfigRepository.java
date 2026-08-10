package com.ltv.stat.repository;

import com.ltv.stat.entity.LtvLaunchConfig;
import com.ltv.stat.entity.LtvLaunchConfigId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LtvLaunchConfigRepository extends JpaRepository<LtvLaunchConfig, LtvLaunchConfigId> {
    Optional<LtvLaunchConfig> findByUserIdAndLaunchDate(Long userId, LocalDate launchDate);
    List<LtvLaunchConfig> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM LtvLaunchConfig c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
