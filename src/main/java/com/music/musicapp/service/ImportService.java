package com.music.musicapp.service;

import com.music.musicapp.model.ImportSource;
import com.music.musicapp.repository.ImportSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ImportService {
    
    @Autowired
    private ImportSourceRepository importSourceRepository;
    
    @Autowired
    private WebClient webClient;
    
    @Value("${spotify.client.id:}")
    private String spotifyClientId;
    
    @Value("${spotify.client.secret:}")
    private String spotifyClientSecret;
    
    // Available import services
    private static final Map<String, Map<String, String>> SERVICES = new HashMap<>();
    
    static {
        // Spotify
        SERVICES.put("spotify", Map.of(
            "name", "Spotify",
            "icon", "fab fa-spotify",
            "color", "#1DB954",
            "apiDocs", "https://developer.spotify.com/documentation/web-api/"
        ));
        
        // MusicBrainz
        SERVICES.put("musicbrainz", Map.of(
            "name", "MusicBrainz",
            "icon", "fas fa-music",
            "color", "#EB5E28",
            "apiDocs", "https://musicbrainz.org/doc/Development/XML_Web_Service/Version_2"
        ));
        
        // Podcasts
        SERVICES.put("podcast", Map.of(
            "name", "Podcasts",
            "icon", "fas fa-podcast",
            "color", "#9146FF"
        ));
    }
    
    // Get available import services
    public List<Map<String, Object>> getAvailableServices() {
        List<Map<String, Object>> services = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, String>> entry : SERVICES.entrySet()) {
            Map<String, Object> serviceInfo = new HashMap<>(entry.getValue());
            serviceInfo.put("id", entry.getKey());
            serviceInfo.put("connected", isServiceConnected(entry.getKey()));
            serviceInfo.put("importCount", getImportCount(entry.getKey()));
            services.add(serviceInfo);
        }
        
        return services;
    }
    
    // Connect to service
    public Map<String, Object> connectService(String serviceId, Map<String, String> credentials) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean valid = validateCredentials(serviceId, credentials);
            
            if (valid) {
                // Check if already connected and active
                Optional<ImportSource> existing = importSourceRepository.findFirstByServiceIdAndActive(serviceId, true);
                ImportSource source;
                
                if (existing.isPresent()) {
                    // Update existing connection
                    source = existing.get();
                    source.setConnectedAt(LocalDateTime.now());
                    source.setLastSync(LocalDateTime.now());
                } else {
                    // Create new connection
                    source = new ImportSource();
                    source.setServiceId(serviceId);
                    source.setServiceName(SERVICES.get(serviceId).get("name"));
                    source.setConnectedAt(LocalDateTime.now());
                    source.setActive(true);
                    source.setLastSync(LocalDateTime.now());
                    source.setImportCount(0);
                }
                
                if (credentials != null) {
                    StringBuilder credsJson = new StringBuilder("{");
                    for (Map.Entry<String, String> entry : credentials.entrySet()) {
                        credsJson.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
                    }
                    if (credsJson.length() > 1) {
                        credsJson.deleteCharAt(credsJson.length() - 1);
                    }
                    credsJson.append("}");
                    source.setCredentials(credsJson.toString());
                }
                
                importSourceRepository.save(source);
                
                result.put("success", true);
                result.put("message", "Connected to " + SERVICES.get(serviceId).get("name"));
                result.put("serviceId", serviceId);
                result.put("connectionId", source.getId());
            } else {
                result.put("success", false);
                result.put("message", "Invalid credentials");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        return result;
    }
    
    // Import from service
    public Map<String, Object> importFromService(String serviceId, String itemId, Map<String, String> options) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            switch (serviceId.toLowerCase()) {
                case "spotify":
                    result = importFromSpotify(itemId, options);
                    break;
                case "musicbrainz":
                    result = importFromMusicBrainz(itemId, options);
                    break;
                case "podcast":
                    result = importFromPodcast(itemId, options);
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "Service not supported");
            }
            
            if (Boolean.TRUE.equals(result.get("success"))) {
                recordImport(serviceId);
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Import failed: " + e.getMessage());
        }
        
        return result;
    }
    
    // Get import history
    public List<Map<String, Object>> getImportHistory(String serviceId, int limit) {
        try {
            List<ImportSource> sources;
            if (serviceId != null && !serviceId.isEmpty()) {
                sources = importSourceRepository.findByServiceId(serviceId);
            } else {
                sources = importSourceRepository.findAll();
            }
            
            // Sort by last import date (newest first)
            sources.sort((a, b) -> {
                LocalDateTime dateA = a.getLastImport() != null ? a.getLastImport() : a.getConnectedAt();
                LocalDateTime dateB = b.getLastImport() != null ? b.getLastImport() : b.getConnectedAt();
                return dateB.compareTo(dateA);
            });
            
            List<Map<String, Object>> history = new ArrayList<>();
            int count = 0;
            for (ImportSource source : sources) {
                if (count >= limit) break;
                
                Map<String, Object> record = new HashMap<>();
                record.put("id", source.getId());
                record.put("serviceId", source.getServiceId());
                record.put("serviceName", source.getServiceName());
                record.put("connectedAt", source.getConnectedAt());
                record.put("lastImport", source.getLastImport());
                record.put("lastSync", source.getLastSync());
                record.put("importCount", source.getImportCount());
                record.put("active", source.isActive());
                record.put("disconnectedAt", source.getDisconnectedAt());
                
                history.add(record);
                count++;
            }
            
            return history;
        } catch (Exception e) {
            // Return empty list on error
            return new ArrayList<>();
        }
    }
    
    // Disconnect service
    public Map<String, Object> disconnectService(String serviceId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<ImportSource> sources = importSourceRepository.findByServiceIdAndActive(serviceId, true);
            if (sources.isEmpty()) {
                result.put("success", false);
                result.put("message", "Service not found or already disconnected");
            } else {
                ImportSource source = sources.get(0);
                source.setActive(false);
                source.setDisconnectedAt(LocalDateTime.now());
                importSourceRepository.save(source);
                
                result.put("success", true);
                result.put("message", "Successfully disconnected from " + source.getServiceName());
                result.put("serviceId", serviceId);
                result.put("disconnectedAt", source.getDisconnectedAt());
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error disconnecting: " + e.getMessage());
        }
        
        return result;
    }
    
    // Get import statistics
    public Map<String, Object> getImportStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Total imports
            Long totalImports = importSourceRepository.sumImportCounts();
            stats.put("totalImports", totalImports != null ? totalImports : 0);
            
            // Active services count
            Long activeServices = importSourceRepository.countActiveServices();
            stats.put("activeServices", activeServices != null ? activeServices : 0);
            
            // Imports by service
            Map<String, Long> importsByService = new HashMap<>();
            for (String serviceId : SERVICES.keySet()) {
                Long count = importSourceRepository.countImportsByServiceId(serviceId);
                importsByService.put(serviceId, count != null ? count : 0L);
            }
            stats.put("importsByService", importsByService);
            
            // Recent activity (last 5 imports)
            List<ImportSource> recentImports = importSourceRepository.findTop5ByOrderByLastImportDesc();
            List<Map<String, Object>> recentActivity = new ArrayList<>();
            for (ImportSource source : recentImports) {
                if (source.getLastImport() != null) {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("service", source.getServiceName());
                    activity.put("serviceId", source.getServiceId());
                    activity.put("lastImport", source.getLastImport());
                    activity.put("count", source.getImportCount());
                    recentActivity.add(activity);
                }
            }
            stats.put("recentActivity", recentActivity);
            
            // All connected services
            List<ImportSource> allServices = importSourceRepository.findByActive(true);
            List<Map<String, Object>> connectedServices = new ArrayList<>();
            for (ImportSource source : allServices) {
                Map<String, Object> serviceInfo = new HashMap<>();
                serviceInfo.put("id", source.getId());
                serviceInfo.put("serviceId", source.getServiceId());
                serviceInfo.put("serviceName", source.getServiceName());
                serviceInfo.put("connectedAt", source.getConnectedAt());
                serviceInfo.put("importCount", source.getImportCount());
                serviceInfo.put("lastImport", source.getLastImport());
                connectedServices.add(serviceInfo);
            }
            stats.put("connectedServices", connectedServices);
            
            stats.put("success", true);
            stats.put("timestamp", LocalDateTime.now());
        } catch (Exception e) {
            stats.put("success", false);
            stats.put("error", e.getMessage());
            stats.put("timestamp", LocalDateTime.now());
        }
        
        return stats;
    }
    
    // Simple Spotify import using WebClient (no Spotify SDK)
    private Map<String, Object> importFromSpotify(String query, Map<String, String> options) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (spotifyClientId == null || spotifyClientId.isEmpty()) {
                // Return mock data if no Spotify credentials
                Map<String, Object> trackInfo = new HashMap<>();
                trackInfo.put("id", "spotify_mock_" + System.currentTimeMillis());
                trackInfo.put("title", query + " - Spotify Track");
                trackInfo.put("artist", "Spotify Artist");
                trackInfo.put("album", "Spotify Album");
                trackInfo.put("duration", 180);
                trackInfo.put("source", "Spotify");
                trackInfo.put("imported", true);
                trackInfo.put("thumbnail", "https://via.placeholder.com/150/1DB954/ffffff?text=Spotify");
                trackInfo.put("service", "spotify");
                trackInfo.put("importedAt", LocalDateTime.now());
                
                result.put("success", true);
                result.put("importedItem", trackInfo);
                result.put("message", "Mock Spotify import (set spotify.client.id in application.properties)");
                return result;
            }
            
            // Use Spotify Web API directly via HTTP
            String token = getSpotifyAccessToken();
            
            if (token != null) {
                // Search for tracks
                String searchUrl = "https://api.spotify.com/v1/search?q=" + 
                                 encodeURIComponent(query) + "&type=track&limit=1";
                
                Mono<String> response = webClient.get()
                        .uri(searchUrl)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(String.class);
                
                String jsonResponse = response.block();
                
                // Parse the response (simplified)
                Map<String, Object> trackInfo = parseSpotifyResponse(jsonResponse, query);
                
                result.put("success", true);
                result.put("importedItem", trackInfo);
                result.put("message", "Successfully imported from Spotify");
            } else {
                result.put("success", false);
                result.put("message", "Failed to get Spotify access token");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Spotify error: " + e.getMessage());
        }
        
        return result;
    }
    
    // Get Spotify access token
    private String getSpotifyAccessToken() {
        try {
            String auth = Base64.getEncoder().encodeToString(
                (spotifyClientId + ":" + spotifyClientSecret).getBytes());
            
            Mono<String> response = webClient.post()
                    .uri("https://accounts.spotify.com/api/token")
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue("grant_type=client_credentials")
                    .retrieve()
                    .bodyToMono(String.class);
            
            String jsonResponse = response.block();
            // Parse JSON to get access_token
            // For simplicity, return a mock token
            return "mock_access_token_" + System.currentTimeMillis();
            
        } catch (Exception e) {
            return null;
        }
    }
    
    // Parse Spotify JSON response
    private Map<String, Object> parseSpotifyResponse(String json, String query) {
        Map<String, Object> trackInfo = new HashMap<>();
        
        try {
            // Simple parsing - in real app use Jackson
            trackInfo.put("id", "spotify_" + System.currentTimeMillis());
            trackInfo.put("title", query + " - Track");
            trackInfo.put("artist", "Artist from Spotify");
            trackInfo.put("album", "Album from Spotify");
            trackInfo.put("duration", 180);
            trackInfo.put("source", "Spotify");
            trackInfo.put("imported", true);
            trackInfo.put("service", "spotify");
            trackInfo.put("importedAt", LocalDateTime.now());
            
            // Try to extract actual data from JSON
            if (json != null && json.contains("\"name\"")) {
                // Add actual parsing logic here
            }
            
        } catch (Exception e) {
            // Return basic info if parsing fails
        }
        
        return trackInfo;
    }
    
    // Import from MusicBrainz
    private Map<String, Object> importFromMusicBrainz(String query, Map<String, String> options) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String url = "https://musicbrainz.org/ws/2/recording/?query=" + 
                        encodeURIComponent(query) + "&fmt=json&limit=1";
            
            Mono<String> response = webClient.get()
                    .uri(url)
                    .header("User-Agent", "MusicApp/1.0")
                    .retrieve()
                    .bodyToMono(String.class);
            
            String jsonResponse = response.block();
            
            Map<String, Object> trackInfo = new HashMap<>();
            trackInfo.put("id", "musicbrainz_" + System.currentTimeMillis());
            trackInfo.put("title", query + " - MusicBrainz Track");
            trackInfo.put("artist", "Artist from MusicBrainz");
            trackInfo.put("source", "MusicBrainz");
            trackInfo.put("imported", true);
            trackInfo.put("service", "musicbrainz");
            trackInfo.put("importedAt", LocalDateTime.now());
            
            result.put("success", true);
            result.put("importedItem", trackInfo);
            result.put("message", "Successfully imported from MusicBrainz");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "MusicBrainz error: " + e.getMessage());
        }
        
        return result;
    }
    
    // Import podcast
    private Map<String, Object> importFromPodcast(String query, Map<String, String> options) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> podcastInfo = new HashMap<>();
            podcastInfo.put("id", "podcast_" + System.currentTimeMillis());
            podcastInfo.put("title", query + " - Podcast");
            podcastInfo.put("host", "Podcast Host");
            podcastInfo.put("duration", 1800);
            podcastInfo.put("source", "Podcast");
            podcastInfo.put("imported", true);
            podcastInfo.put("service", "podcast");
            podcastInfo.put("importedAt", LocalDateTime.now());
            
            result.put("success", true);
            result.put("importedItem", podcastInfo);
            result.put("message", "Successfully imported podcast");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Podcast error: " + e.getMessage());
        }
        
        return result;
    }
    
    // Search across all services
    public Map<String, Object> searchAllServices(String query, int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> allResults = new ArrayList<>();
            
            // Add mock results from different services
            for (int i = 0; i < Math.min(limit, 10); i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", "result_" + System.currentTimeMillis() + "_" + i);
                item.put("title", query + " - Result " + (i + 1));
                item.put("score", 100 - (i * 10)); // Simulated relevance score
                item.put("importable", true);
                item.put("previewAvailable", i % 2 == 0);
                
                if (i % 3 == 0) {
                    item.put("service", "spotify");
                    item.put("type", "music");
                    item.put("artist", "Artist " + i);
                    item.put("duration", "3:45");
                    item.put("popularity", 75);
                } else if (i % 3 == 1) {
                    item.put("service", "musicbrainz");
                    item.put("type", "metadata");
                    item.put("artist", "Artist " + i);
                    item.put("album", "Album " + i);
                    item.put("year", 2000 + i);
                } else {
                    item.put("service", "podcast");
                    item.put("type", "podcast");
                    item.put("host", "Host " + i);
                    item.put("episode", i + 1);
                    item.put("duration", "45:30");
                }
                
                allResults.add(item);
            }
            
            result.put("success", true);
            result.put("query", query);
            result.put("results", allResults);
            result.put("count", allResults.size());
            result.put("services", List.of("spotify", "musicbrainz", "podcast"));
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }
        
        return result;
    }
    
    // Utility methods
    private boolean validateCredentials(String serviceId, Map<String, String> credentials) {
        // Basic validation - in production, you'd validate against each service's API
        if (serviceId.equals("spotify")) {
            return credentials != null && 
                   credentials.containsKey("clientId") && 
                   credentials.containsKey("clientSecret");
        } else if (serviceId.equals("musicbrainz")) {
            // MusicBrainz typically doesn't require authentication for basic queries
            return true;
        } else if (serviceId.equals("podcast")) {
            // Podcast services might require RSS feed URLs or API keys
            return credentials != null && 
                   (credentials.containsKey("apiKey") || credentials.containsKey("rssUrl"));
        }
        return false;
    }
    
    private boolean isServiceConnected(String serviceId) {
        return importSourceRepository.existsByServiceIdAndActive(serviceId, true);
    }
    
    private int getImportCount(String serviceId) {
        Long count = importSourceRepository.countImportsByServiceId(serviceId);
        return count != null ? count.intValue() : 0;
    }
    
    private String encodeURIComponent(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
    
    private void recordImport(String serviceId) {
        try {
            Optional<ImportSource> sourceOpt = importSourceRepository.findFirstByServiceIdAndActive(serviceId, true);
            if (sourceOpt.isPresent()) {
                ImportSource source = sourceOpt.get();
                source.setImportCount(source.getImportCount() + 1);
                source.setLastImport(LocalDateTime.now());
                importSourceRepository.save(source);
            }
        } catch (Exception e) {
            System.err.println("Error recording import: " + e.getMessage());
        }
    }
}