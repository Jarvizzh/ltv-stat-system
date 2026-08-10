package com.ltv.stat.repository;

import com.ltv.stat.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
    boolean existsByUsername(String username);
    List<SysUser> findByStatusOrderByCreatedAtDesc(Integer status);
    List<SysUser> findAllByOrderByCreatedAtDesc();
}
