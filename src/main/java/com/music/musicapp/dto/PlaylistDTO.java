package com.music.musicapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDTO {
    private Long id;
    private String name;
    private String description;
    private String username;
    private Long userId;
    private boolean isPublic;
    private List<SongDTO> songs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int songCount;
    private int followerCount;
}