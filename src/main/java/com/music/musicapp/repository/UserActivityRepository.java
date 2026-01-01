package com.music.musicapp.repository;

import com.music.musicapp.model.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    
    List<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT a FROM UserActivity a WHERE a.user.id IN :userIds ORDER BY a.createdAt DESC")
    List<UserActivity> findByUserIdsOrderByCreatedAtDesc(@Param("userIds") List<Long> userIds);
    
    @Query("SELECT a FROM UserActivity a WHERE a.user.id = :userId AND a.activityType = :activityType ORDER BY a.createdAt DESC")
    List<UserActivity> findByUserIdAndActivityType(@Param("userId") Long userId, @Param("activityType") String activityType);
}