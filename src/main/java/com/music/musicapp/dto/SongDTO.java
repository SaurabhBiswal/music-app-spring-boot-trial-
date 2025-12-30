package com.music.musicapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongDTO {
    private Long id;
    private String title;
    private String artist;
    private String album;
    private Integer releaseYear;
    private Integer durationSeconds;
    private String formattedDuration; // MM:SS format
    private String filePath;
    private String genre;
    private String uploadedAt;
    
    // Constructor from Song entity
    public SongDTO(Long id, String title, String artist, String album, 
                   Integer releaseYear, Integer durationSeconds, 
                   String filePath, String genre, String uploadedAt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.releaseYear = releaseYear;
        this.durationSeconds = durationSeconds;
        this.filePath = filePath;
        this.genre = genre;
        this.uploadedAt = uploadedAt;
        
        // Calculate formatted duration
        if (durationSeconds != null) {
            int minutes = durationSeconds / 60;
            int seconds = durationSeconds % 60;
            this.formattedDuration = String.format("%d:%02d", minutes, seconds);
        } else {
            this.formattedDuration = "0:00";
        }
    }
}