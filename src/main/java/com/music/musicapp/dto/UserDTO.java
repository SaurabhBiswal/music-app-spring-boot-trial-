package com.music.musicapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;  // Changed from long to Long (capital L)
    private String username;
    private String email;
    private String fullName;
    private String createdAt;
    
    // Remove the duplicate constructor below - @AllArgsConstructor already creates it
    // public UserDTO(Long id, String username, String email, String fullName, String createdAt) {
    //     this.id = id;
    //     this.username = username;
    //     this.email = email;
    //     this.fullName = fullName;
    //     this.createdAt = createdAt;
    // }
}