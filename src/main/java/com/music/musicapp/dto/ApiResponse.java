package com.music.musicapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private String status;
    private String message;
    private Object data;
    
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse("success", message, data);
    }
    
    public static ApiResponse error(String message) {
        return new ApiResponse("error", message, null);
    }
}