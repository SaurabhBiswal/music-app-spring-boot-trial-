package com.music.musicapp.service;

import com.music.musicapp.model.CacheItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
@Service
public class CacheService {
    
    @Value("${cache.storage.path:./cache}")
    private String cacheStoragePath;
    
    @Value("${cache.enabled:true}")
    private boolean cacheEnabled;
    
    @Value("${cache.max.size.mb:1024}")
    private int maxCacheSizeMB;
    
    // In-memory cache for quick access
    private final Map<String, CacheItem> memoryCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> objectCache = new ConcurrentHashMap<>();
    
    public CacheService() {
        initializeCacheDirectory();
        loadPersistentCache();
    }
    
    // Initialize cache directory
    private void initializeCacheDirectory() {
        if (!cacheEnabled) return;
        
        try {
            Path cachePath = Paths.get(cacheStoragePath);
            if (!Files.exists(cachePath)) {
                Files.createDirectories(cachePath);
                System.out.println("Created cache directory: " + cachePath.toAbsolutePath());
            }
            
            // Create subdirectories
            Files.createDirectories(cachePath.resolve("audio"));
            Files.createDirectories(cachePath.resolve("images"));
            Files.createDirectories(cachePath.resolve("metadata"));
            
        } catch (Exception e) {
            System.err.println("Error initializing cache directory: " + e.getMessage());
        }
    }
    
    // Load persistent cache from disk
    private void loadPersistentCache() {
        if (!cacheEnabled) return;
        
        try {
            Path cacheFile = Paths.get(cacheStoragePath, "cache_index.json");
            if (Files.exists(cacheFile)) {
                String content = Files.readString(cacheFile);
                // Parse JSON and load into memory cache
                // Simplified for demo
                System.out.println("Loaded persistent cache index");
            }
        } catch (Exception e) {
            System.err.println("Error loading persistent cache: " + e.getMessage());
        }
    }
    
    // Save to cache
    public void saveToCache(String key, List<Map<String, Object>> data, int ttlSeconds) {
        if (!cacheEnabled) return;
        
        try {
            CacheItem cacheItem = new CacheItem();
            cacheItem.setKey(key);
            cacheItem.setData(data);
            cacheItem.setTimestamp(LocalDateTime.now());
            cacheItem.setTtlSeconds(ttlSeconds);
            
            // Store in memory
            memoryCache.put(key, cacheItem);
            
            // Also persist to disk
            persistCacheItem(key, cacheItem);
            
            // Clean up old cache if needed
            cleanupCache();
            
        } catch (Exception e) {
            System.err.println("Error saving to cache: " + e.getMessage());
        }
    }
    
    // Save object to cache
   public boolean saveObjectToCache(String key, Object data, int ttlSeconds) {
    if (!cacheEnabled) return false;
    
    try {
        CacheItem cacheItem = new CacheItem();
        cacheItem.setKey(key);
        cacheItem.setObjectData(data);
        cacheItem.setTimestamp(LocalDateTime.now());
        cacheItem.setTtlSeconds(ttlSeconds);
        
        // Store in memory
        memoryCache.put(key, cacheItem);
        
        // Also persist to disk
        persistCacheItem(key, cacheItem);
        
        return true; // Return success status
        
    } catch (Exception e) {
        System.err.println("Error saving object to cache: " + e.getMessage());
        return false;
    }
}
    
    // Get from cache
    public List<Map<String, Object>> getFromCache(String key) {
        if (!cacheEnabled) return null;
        
        try {
            CacheItem cacheItem = memoryCache.get(key);
            if (cacheItem != null && !isExpired(cacheItem)) {
                return cacheItem.getData();
            } else {
                // Remove expired item
                memoryCache.remove(key);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error getting from cache: " + e.getMessage());
            return null;
        }
    }
    
    // Get object from cache
    public Object getObjectFromCache(String key) {
        if (!cacheEnabled) return null;
        
        try {
            CacheItem cacheItem = memoryCache.get(key);
            if (cacheItem != null && !isExpired(cacheItem)) {
                return cacheItem.getObjectData();
            } else {
                memoryCache.remove(key);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error getting object from cache: " + e.getMessage());
            return null;
        }
    }
    
    // Check if cache item is expired
    private boolean isExpired(CacheItem cacheItem) {
        if (cacheItem.getTtlSeconds() <= 0) return false; // Never expires
        
        LocalDateTime expirationTime = cacheItem.getTimestamp()
            .plusSeconds(cacheItem.getTtlSeconds());
        return LocalDateTime.now().isAfter(expirationTime);
    }
    
    // Persist cache item to disk
    private void persistCacheItem(String key, CacheItem cacheItem) {
        if (!cacheEnabled) return;
        
        try {
            // Serialize and save to file
            Path cacheFile = Paths.get(cacheStoragePath, "items", key + ".cache");
            Files.createDirectories(cacheFile.getParent());
            
            // Simplified serialization (in production, use JSON or binary)
            String serialized = key + "|" + cacheItem.getTimestamp() + "|" + 
                              cacheItem.getTtlSeconds();
            Files.writeString(cacheFile, serialized);
            
        } catch (Exception e) {
            System.err.println("Error persisting cache item: " + e.getMessage());
        }
    }
    
    // Cleanup old cache
    private void cleanupCache() {
        if (!cacheEnabled) return;
        
        try {
            long currentTime = System.currentTimeMillis();
            long maxAge = 7 * 24 * 60 * 60 * 1000L; // 7 days
            
            // Clean memory cache
            Iterator<Map.Entry<String, CacheItem>> iterator = memoryCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CacheItem> entry = iterator.next();
                CacheItem item = entry.getValue();
                
                if (isExpired(item)) {
                    iterator.remove();
                }
            }
            
            // Clean disk cache
            Path cacheDir = Paths.get(cacheStoragePath, "items");
            if (Files.exists(cacheDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDir, "*.cache")) {
                    for (Path file : stream) {
                        try {
                            long fileAge = Files.getLastModifiedTime(file).toMillis();
                            if (currentTime - fileAge > maxAge) {
                                Files.delete(file);
                            }
                        } catch (Exception e) {
                            System.err.println("Error cleaning cache file: " + e.getMessage());
                        }
                    }
                }
            }
            
            // Check cache size
            checkCacheSize();
            
        } catch (Exception e) {
            System.err.println("Error cleaning cache: " + e.getMessage());
        }
    }
    
    // Check and limit cache size
    private void checkCacheSize() {
        try {
            long totalSize = getCacheSize();
            long maxSizeBytes = maxCacheSizeMB * 1024L * 1024L;
            
            if (totalSize > maxSizeBytes) {
                System.out.println("Cache size limit exceeded. Cleaning up...");
                
                // Sort files by last modified (oldest first)
                List<Path> cacheFiles = getCacheFilesSortedByAge();
                
                // Delete oldest files until under limit
                for (Path file : cacheFiles) {
                    long fileSize = Files.size(file);
                    Files.delete(file);
                    totalSize -= fileSize;
                    
                    if (totalSize <= maxSizeBytes * 0.8) { // Target 80% of max
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking cache size: " + e.getMessage());
        }
    }
    
    // Get total cache size
    private long getCacheSize() throws IOException {
        long totalSize = 0;
        Path cacheDir = Paths.get(cacheStoragePath);
        
        if (Files.exists(cacheDir)) {
            try (var walk = Files.walk(cacheDir)) {
                totalSize = walk
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
            }
        }
        
        return totalSize;
    }
    
    // Get cache files sorted by age (oldest first)
    private List<Path> getCacheFilesSortedByAge() throws IOException {
        List<Path> files = new ArrayList<>();
        Path cacheDir = Paths.get(cacheStoragePath);
        
        if (Files.exists(cacheDir)) {
            try (var walk = Files.walk(cacheDir)) {
                files = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".cache") || 
                               p.toString().endsWith(".mp3") || 
                               p.toString().endsWith(".jpg"))
                    .sorted(Comparator.comparing(p -> {
                        try {
                            return Files.getLastModifiedTime(p).toMillis();
                        } catch (IOException e) {
                            return 0L;
                        }
                    }))
                    .collect(Collectors.toList());
            }
        }
        
        return files;
    }
    
    // Download and cache audio file
    public void downloadAndCacheAudio(String trackId, String audioUrl) {
        if (!cacheEnabled) return;
        
        try {
            Path audioFile = Paths.get(cacheStoragePath, "audio", trackId + ".mp3");
            
            // Check if already cached
            if (Files.exists(audioFile)) {
                System.out.println("Audio already cached: " + trackId);
                return;
            }
            
            System.out.println("Downloading audio for offline caching: " + trackId);
            
            // In production, download the file
            // For demo, create a placeholder file
            Files.createDirectories(audioFile.getParent());
            String placeholder = "Offline audio placeholder for: " + trackId;
            Files.writeString(audioFile, placeholder);
            
            // Store metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("trackId", trackId);
            metadata.put("audioUrl", audioUrl);
            metadata.put("cachedAt", LocalDateTime.now().toString());
            metadata.put("filePath", audioFile.toString());
            
            saveObjectToCache("offline_" + trackId, metadata, 2592000); // 30 days
            
            System.out.println("Audio cached for offline: " + trackId);
            
        } catch (Exception e) {
            System.err.println("Error caching audio: " + e.getMessage());
        }
    }
    
    // Get offline cached audio path
    public String getOfflineAudioPath(String trackId) {
        if (!cacheEnabled) return null;
        
        try {
            Path audioFile = Paths.get(cacheStoragePath, "audio", trackId + ".mp3");
            if (Files.exists(audioFile)) {
                return audioFile.toAbsolutePath().toString();
            }
        } catch (Exception e) {
            System.err.println("Error getting offline audio: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get list of offline track IDs
    public List<String> getOfflineKeys() {
        List<String> keys = new ArrayList<>();
        
        if (!cacheEnabled) return keys;
        
        try {
            // Get from memory cache
            for (String key : memoryCache.keySet()) {
                if (key.startsWith("offline_")) {
                    keys.add(key.substring(8)); // Remove "offline_" prefix
                }
            }
            
            // Also check disk
            Path audioDir = Paths.get(cacheStoragePath, "audio");
            if (Files.exists(audioDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(audioDir, "*.mp3")) {
                    for (Path file : stream) {
                        String fileName = file.getFileName().toString();
                        String trackId = fileName.substring(0, fileName.length() - 4);
                        if (!keys.contains(trackId)) {
                            keys.add(trackId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting offline keys: " + e.getMessage());
        }
        
        return keys;
    }
    
    // Clear cache
    public void clearCache() {
        if (!cacheEnabled) return;
        
        try {
            // Clear memory cache
            memoryCache.clear();
            objectCache.clear();
            
            // Clear disk cache
            Path cacheDir = Paths.get(cacheStoragePath);
            if (Files.exists(cacheDir)) {
                deleteDirectory(cacheDir);
                Files.createDirectories(cacheDir);
            }
            
            System.out.println("Cache cleared successfully");
            
        } catch (Exception e) {
            System.err.println("Error clearing cache: " + e.getMessage());
        }
    }
    
    // Delete directory recursively
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            try (var walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            System.err.println("Error deleting: " + p);
                        }
                    });
            }
        }
    }
    
    // Get cache statistics
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        if (!cacheEnabled) {
            stats.put("enabled", false);
            return stats;
        }
        
        try {
            stats.put("enabled", true);
            stats.put("memoryCacheSize", memoryCache.size());
            stats.put("objectCacheSize", objectCache.size());
            stats.put("storagePath", cacheStoragePath);
            
            long totalSize = getCacheSize();
            stats.put("totalSizeBytes", totalSize);
            stats.put("totalSizeMB", totalSize / (1024 * 1024));
            stats.put("maxSizeMB", maxCacheSizeMB);
            
            // Count offline tracks
            List<String> offlineKeys = getOfflineKeys();
            stats.put("offlineTrackCount", offlineKeys.size());
            
            // Get oldest and newest cache items
            if (!memoryCache.isEmpty()) {
                Optional<CacheItem> oldest = memoryCache.values().stream()
                    .min(Comparator.comparing(CacheItem::getTimestamp));
                Optional<CacheItem> newest = memoryCache.values().stream()
                    .max(Comparator.comparing(CacheItem::getTimestamp));
                
                oldest.ifPresent(item -> stats.put("oldestCache", item.getTimestamp()));
                newest.ifPresent(item -> stats.put("newestCache", item.getTimestamp()));
            }
            
        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
}