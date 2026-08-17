package com.ltv.stat.repository;

import com.ltv.stat.entity.UserSubAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubAccountRepository extends JpaRepository<UserSubAccount, Long> {

    List<UserSubAccount> findByMasterUserId(Long masterUserId);

    List<UserSubAccount> findBySubUserId(Long subUserId);

    boolean existsByMasterUserIdAndSubUserId(Long masterUserId, Long subUserId);

    void deleteByMasterUserId(Long masterUserId);

    void deleteByMasterUserIdAndSubUserId(Long masterUserId, Long subUserId);

    void deleteBySubUserId(Long subUserId);
}
