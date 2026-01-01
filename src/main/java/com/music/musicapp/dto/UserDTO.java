package com.music.musicapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Integer followerCount;
    private Integer followingCount;
    private Integer playlistCount;
    private Integer reviewCount;
}