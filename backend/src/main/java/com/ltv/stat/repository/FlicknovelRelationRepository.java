package com.ltv.stat.repository;

import com.ltv.stat.entity.FlicknovelRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlicknovelRelationRepository extends JpaRepository<FlicknovelRelation, Long> {

    Optional<FlicknovelRelation> findByRelationId(String relationId);

    Optional<FlicknovelRelation> findFirstByDeviceIdOrderByRelationBeginTimeBjAsc(String deviceId);

    List<FlicknovelRelation> findByRelationBeginTimeBjBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByRelationId(String relationId);

    List<FlicknovelRelation> findByRelationIdIn(List<String> relationIds);
}
