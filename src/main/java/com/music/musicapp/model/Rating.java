package com.music.musicapp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Data
public class Rating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "track_id")
    private String trackId; // For external tracks
    
    @Column(name = "song_id")
    private Long songId; // For internal songs
    
    @Column(name = "rating_value", nullable = false)
    private Integer ratingValue;
    
    @Column(name = "comment")
    private String comment;
    
    @Column(name = "rated_at", nullable = false)
    private LocalDateTime ratedAt;
    
    @PrePersist
    protected void onCreate() {
        ratedAt = LocalDateTime.now();
    }
}