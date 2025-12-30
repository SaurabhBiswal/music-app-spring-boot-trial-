package com.music.musicapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "songs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Song {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String artist;
    
    private String album;
    
    @Column(name = "release_year")
    private Integer releaseYear;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "file_path")
    private String filePath;
    
    private String genre;
    
    @Column(name = "uploaded_at")
    private String uploadedAt;
    
    // Constructor without id
    public Song(String title, String artist, String album, Integer durationSeconds, 
                String filePath, String genre) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.durationSeconds = durationSeconds;
        this.filePath = filePath;
        this.genre = genre;
        this.uploadedAt = java.time.LocalDateTime.now().toString();
    }
    
    // Helper method to get duration in MM:SS format
    public String getFormattedDuration() {
        if (durationSeconds == null) return "0:00";
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}