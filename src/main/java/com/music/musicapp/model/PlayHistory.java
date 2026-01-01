package com.music.musicapp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "play_history")
@Data
public class PlayHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "song_id", nullable = false)
    private Long songId;
    
    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;
    
    @Column(name = "play_duration_seconds")
    private Integer playDurationSeconds;
    
    @PrePersist
    protected void onCreate() {
        playedAt = LocalDateTime.now();
    }
}