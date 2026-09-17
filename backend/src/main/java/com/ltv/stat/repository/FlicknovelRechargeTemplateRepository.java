package com.ltv.stat.repository;

import com.ltv.stat.entity.FlicknovelRechargeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlicknovelRechargeTemplateRepository extends JpaRepository<FlicknovelRechargeTemplate, Long> {

    Optional<FlicknovelRechargeTemplate> findByTemplateId(String templateId);

    List<FlicknovelRechargeTemplate> findByTemplateIdIn(List<String> templateIds);

    boolean existsByTemplateId(String templateId);
}
