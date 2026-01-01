package com.music.musicapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AudioProcessingException extends RuntimeException {
    
    public AudioProcessingException(String message) {
        super(message);
    }
    
    public AudioProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}