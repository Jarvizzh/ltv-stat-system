package com.ltv.stat.repository;

import com.ltv.stat.entity.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, Long> {

    Optional<PlatformConfig> findByPlatformCode(String platformCode);

    List<PlatformConfig> findByStatusOrderByCreatedAtAsc(Integer status);
}
