package com.music.musicapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MusicDiscoveryService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PodcastService podcastService;
    private final CacheService cacheService;
    
    // Cache for offline content
    private final Map<String, Map<String, Object>> offlineCache = new ConcurrentHashMap<>();
    
    @Value("${music.discovery.enabled:true}")
    private boolean discoveryEnabled;
    
    @Value("${music.cache.enabled:true}")
    private boolean cacheEnabled;
    
    @Value("${music.cache.duration:86400}") // 24 hours
    private int cacheDuration;
    
    public MusicDiscoveryService(PodcastService podcastService, CacheService cacheService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.podcastService = podcastService;
        this.cacheService = cacheService;
        
        // Set headers for MusicBrainz API
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", "MusicApp/1.0 (contact@musichub.com)");
            request.getHeaders().set("Accept", "application/json");
            return execution.execute(request, body);
        });
    }
    
    // SIMPLIFIED: Search music from MusicBrainz
    public List<Map<String, Object>> searchMusic(String query, int limit) {
        if (!discoveryEnabled) {
            return Collections.emptyList();
        }
        
        String cacheKey = "music_search_" + query + "_" + limit;
        
        // Check cache first
        if (cacheEnabled) {
            List<Map<String, Object>> cached = cacheService.getFromCache(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // Search MusicBrainz
            results = searchMusicBrainz(query, limit);
            
            // Cache results
            if (cacheEnabled && !results.isEmpty()) {
                cacheService.saveToCache(cacheKey, results, cacheDuration);
            }
            
        } catch (Exception e) {
            System.err.println("Error searching music: " + e.getMessage());
            // Return mock data if API fails
            results = getMockMusicData(query, limit);
        }
        
        return results;
    }
    
    // Search MusicBrainz
    private List<Map<String, Object>> searchMusicBrainz(String query, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            String url = "https://musicbrainz.org/ws/2/recording";
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("query", query)
                .queryParam("fmt", "json")
                .queryParam("limit", limit);
            
            ResponseEntity<String> response = restTemplate.getForEntity(
                builder.toUriString(), 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode recordings = root.path("recordings");
                
                for (JsonNode recording : recordings) {
                    Map<String, Object> track = new HashMap<>();
                    
                    // Basic track info
                    track.put("id", "mb_" + recording.path("id").asText());
                    track.put("title", recording.path("title").asText());
                    track.put("duration", recording.path("length").asInt() / 1000);
                    
                    // Artist info
                    JsonNode artistCredit = recording.path("artist-credit");
                    if (artistCredit.isArray() && artistCredit.size() > 0) {
                        JsonNode artist = artistCredit.get(0).path("artist");
                        track.put("artist", artist.path("name").asText());
                        track.put("artistId", artist.path("id").asText());
                    }
                    
                    // Album info
                    JsonNode releases = recording.path("releases");
                    if (releases.isArray() && releases.size() > 0) {
                        JsonNode release = releases.get(0);
                        track.put("album", release.path("title").asText());
                        track.put("year", release.path("date").asText().substring(0, 4));
                    }
                    
                    track.put("source", "MusicBrainz");
                    track.put("type", "music");
                    
                    results.add(track);
                }
            }
        } catch (Exception e) {
            System.err.println("Error searching MusicBrainz: " + e.getMessage());
        }
        
        return results;
    }
    
    // Mock music data (fallback)
    private List<Map<String, Object>> getMockMusicData(String query, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Popular music tracks
        String[][] tracks = {
            {"Imagine", "John Lennon", "1971", "180"},
            {"Bohemian Rhapsody", "Queen", "1975", "354"},
            {"Hotel California", "Eagles", "1976", "391"},
            {"Stairway to Heaven", "Led Zeppelin", "1971", "482"},
            {"Billie Jean", "Michael Jackson", "1982", "294"},
            {"Like a Rolling Stone", "Bob Dylan", "1965", "369"},
            {"Smells Like Teen Spirit", "Nirvana", "1991", "301"},
            {"What's Going On", "Marvin Gaye", "1971", "233"},
            {"Good Vibrations", "The Beach Boys", "1966", "216"},
            {"Johnny B. Goode", "Chuck Berry", "1958", "161"}
        };
        
        int count = 0;
        for (String[] track : tracks) {
            if (count >= limit) break;
            
            if (query.isEmpty() || 
                track[0].toLowerCase().contains(query.toLowerCase()) || 
                track[1].toLowerCase().contains(query.toLowerCase())) {
                
                Map<String, Object> trackData = new HashMap<>();
                trackData.put("id", "mock_" + track[0].replace(" ", "_").toLowerCase());
                trackData.put("title", track[0]);
                trackData.put("artist", track[1]);
                trackData.put("year", track[2]);
                trackData.put("duration", Integer.parseInt(track[3]));
                trackData.put("album", "Greatest Hits");
                trackData.put("source", "Music Library");
                trackData.put("type", "music");
                
                results.add(trackData);
                count++;
            }
        }
        
        // If no matches, return some default tracks
        if (results.isEmpty()) {
            for (int i = 0; i < Math.min(limit, 5); i++) {
                Map<String, Object> trackData = new HashMap<>();
                trackData.put("id", "track_" + (i + 1));
                trackData.put("title", query + " - Song " + (i + 1));
                trackData.put("artist", "Artist " + (char)('A' + i));
                trackData.put("year", "2023");
                trackData.put("duration", 180 + (i * 30));
                trackData.put("album", "Demo Album");
                trackData.put("source", "Music Library");
                trackData.put("type", "music");
                results.add(trackData);
            }
        }
        
        return results;
    }
    
    // Get trending music (simplified)
    public List<Map<String, Object>> getTrendingMusic() {
        String cacheKey = "trending_music";
        
        if (cacheEnabled) {
            List<Map<String, Object>> cached = cacheService.getFromCache(cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        
        // Return popular tracks
        List<Map<String, Object>> trending = getMockMusicData("", 10);
        
        // Cache results
        if (cacheEnabled) {
            cacheService.saveToCache(cacheKey, trending, 3600); // 1 hour
        }
        
        return trending;
    }
    
    // Get available genres
    public List<Map<String, Object>> getAvailableGenres() {
        return Arrays.asList(
            createGenre("pop", "Pop", "#FF6B6B"),
            createGenre("rock", "Rock", "#4ECDC4"),
            createGenre("hiphop", "Hip-Hop", "#FFD166"),
            createGenre("jazz", "Jazz", "#06D6A0"),
            createGenre("classical", "Classical", "#118AB2"),
            createGenre("electronic", "Electronic", "#EF476F"),
            createGenre("country", "Country", "#7209B7")
        );
    }
    
    private Map<String, Object> createGenre(String id, String name, String color) {
        Map<String, Object> genre = new HashMap<>();
        genre.put("id", id);
        genre.put("name", name);
        genre.put("color", color);
        genre.put("trackCount", new Random().nextInt(1000) + 100);
        return genre;
    }
    
    // Search podcasts (delegates to PodcastService)
    public List<Map<String, Object>> searchPodcasts(String query, int limit) {
        return podcastService.searchPodcasts(query, limit);
    }
    
    // Combined search (music + podcasts)
    public Map<String, Object> searchAll(String query, int limit) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("query", query);
        result.put("music", searchMusic(query, limit));
        result.put("podcasts", searchPodcasts(query, limit));
        result.put("timestamp", new Date());
        result.put("status", "success");
        
        return result;
    }
    
    // Get homepage data
    public Map<String, Object> getHomeData() {
        Map<String, Object> data = new HashMap<>();
        
        data.put("trendingMusic", getTrendingMusic());
        data.put("categories", getAvailableGenres());
        data.put("featuredPodcasts", podcastService.searchPodcasts("technology", 3));
        data.put("timestamp", new Date());
        data.put("status", "success");
        
        return data;
    }
    
    // Get podcast episodes
    public List<Map<String, Object>> getPodcastEpisodes(String podcastId, int limit) {
        return podcastService.getPodcastEpisodes(podcastId, limit);
    }
    
    // Get podcast categories
    public List<Map<String, Object>> getPodcastCategories() {
        return podcastService.getPodcastCategories();
    }
}