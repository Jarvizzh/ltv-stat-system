package com.ltv.stat.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ltv_predict_benchmark", uniqueConstraints = {
    @UniqueConstraint(name = "uk_dim_period_day", columnNames = {"dimension_type", "dimension_value", "sub_period_days", "day_index"})
})
public class LtvPredictBenchmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dimension_type", nullable = false, length = 32)
    private String dimensionType = "ALL";

    @Column(name = "dimension_value", nullable = false, length = 64)
    private String dimensionValue = "DEFAULT";

    @Column(name = "sub_period_days", nullable = false)
    private Integer subPeriodDays = 1;

    @Column(name = "day_index", nullable = false)
    private Integer dayIndex;

    @Column(name = "base_retention_rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal baseRetentionRate = BigDecimal.ZERO;

    @Column(name = "base_arpu", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseArpu = BigDecimal.ZERO;

    @Column(name = "sample_cohort_count", nullable = false)
    private Integer sampleCohortCount = 0;

    @Column(name = "is_extrapolated", nullable = false)
    private Integer isExtrapolated = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDimensionType() { return dimensionType; }
    public void setDimensionType(String dimensionType) { this.dimensionType = dimensionType; }

    public String getDimensionValue() { return dimensionValue; }
    public void setDimensionValue(String dimensionValue) { this.dimensionValue = dimensionValue; }

    public Integer getSubPeriodDays() { return subPeriodDays != null ? subPeriodDays : 1; }
    public void setSubPeriodDays(Integer subPeriodDays) { this.subPeriodDays = subPeriodDays; }

    public Integer getDayIndex() { return dayIndex; }
    public void setDayIndex(Integer dayIndex) { this.dayIndex = dayIndex; }

    public BigDecimal getBaseRetentionRate() { return baseRetentionRate; }
    public void setBaseRetentionRate(BigDecimal baseRetentionRate) { this.baseRetentionRate = baseRetentionRate; }

    public BigDecimal getBaseArpu() { return baseArpu; }
    public void setBaseArpu(BigDecimal baseArpu) { this.baseArpu = baseArpu; }

    public Integer getSampleCohortCount() { return sampleCohortCount; }
    public void setSampleCohortCount(Integer sampleCohortCount) { this.sampleCohortCount = sampleCohortCount; }

    public Integer getIsExtrapolated() { return isExtrapolated; }
    public void setIsExtrapolated(Integer isExtrapolated) { this.isExtrapolated = isExtrapolated; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
