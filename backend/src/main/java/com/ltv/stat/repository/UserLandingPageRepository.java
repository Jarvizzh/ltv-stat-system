package com.ltv.stat.repository;

import com.ltv.stat.entity.UserLandingPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLandingPageRepository extends JpaRepository<UserLandingPage, Long> {
    List<UserLandingPage> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserLandingPage p WHERE p.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndLandingPageId(Long userId, String landingPageId);
}
