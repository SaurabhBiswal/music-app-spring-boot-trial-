package com.music.musicapp.repository;

import com.music.musicapp.model.PasswordResetToken;
import com.music.musicapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, Long> {
    
    // Find token by token string
    Optional<PasswordResetToken> findByToken(String token);
    
    // Find token by user
    Optional<PasswordResetToken> findByUser(User user);
    
    // Delete by user
    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.user = :user")
    void deleteByUser(User user);
    
    // Delete by token
    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.token = :token")
    void deleteByToken(String token);
    
    // ✅ SINGLE METHOD for deleting expired tokens
    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :currentDate")
    void deleteExpiredTokens(@Param("currentDate") Date currentDate);
    
    // Helper method to delete expired tokens with current date
    default void deleteExpiredTokens() {
        deleteExpiredTokens(new Date());
    }
}