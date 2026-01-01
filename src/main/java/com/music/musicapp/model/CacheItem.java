package com.music.musicapp.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CacheItem {
    private String key;
    private List<Map<String, Object>> data;
    private Object objectData;
    private LocalDateTime timestamp;
    private int ttlSeconds; // Time to live in seconds
    private long size; // Size in bytes
}