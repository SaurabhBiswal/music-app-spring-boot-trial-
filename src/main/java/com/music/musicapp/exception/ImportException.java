package com.music.musicapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ImportException extends RuntimeException {
    
    private final String serviceName;
    private final String errorCode;
    
    public ImportException(String message) {
        super(message);
        this.serviceName = "Unknown";
        this.errorCode = "UNKNOWN";
    }
    
    public ImportException(String serviceName, String errorCode, String message) {
        super(String.format("Import from %s failed [%s]: %s", serviceName, errorCode, message));
        this.serviceName = serviceName;
        this.errorCode = errorCode;
    }
}