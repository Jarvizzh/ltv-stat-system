package com.ltv.stat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sub_account", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"master_user_id", "sub_user_id"})
})
public class UserSubAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_user_id", nullable = false)
    private Long masterUserId;

    @Column(name = "sub_user_id", nullable = false)
    private Long subUserId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public UserSubAccount() {
    }

    public UserSubAccount(Long masterUserId, Long subUserId) {
        this.masterUserId = masterUserId;
        this.subUserId = subUserId;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMasterUserId() {
        return masterUserId;
    }

    public void setMasterUserId(Long masterUserId) {
        this.masterUserId = masterUserId;
    }

    public Long getSubUserId() {
        return subUserId;
    }

    public void setSubUserId(Long subUserId) {
        this.subUserId = subUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
