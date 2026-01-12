package com.music.musicapp.repository;

import com.music.musicapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by username
    Optional<User> findByUsername(String username);
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // ✅ FIXED: Method with TWO parameters (username, email)
    @Query("SELECT u FROM User u WHERE u.username = ?1 OR u.email = ?2")
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // ✅ Optional: Method with ONE parameter (username or email)
    @Query("SELECT u FROM User u WHERE u.username = ?1 OR u.email = ?1")
    Optional<User> findByUsernameOrEmail(String identifier);
    
    // Get recent users
    List<User> findTop5ByOrderByIdDesc();
}