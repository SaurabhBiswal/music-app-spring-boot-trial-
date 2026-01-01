package com.music.musicapp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_activities")
@Data
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "activity_type", nullable = false)
    private String activityType; // "RATED", "SHARED", "FOLLOWED", "COMMENTED", "ADDED", "LISTENED"
    
    @Column(name = "target_type")
    private String targetType; // "TRACK", "PLAYLIST", "USER", "REVIEW"
    
    @Column(name = "target_id")
    private String targetId;
    
    @Column(name = "target_name")
    private String targetName;
    
    @Column(name = "details", length = 500)
    private String details;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}