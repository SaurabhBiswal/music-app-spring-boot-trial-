package com.music.musicapp.controller;

import com.music.musicapp.dto.ApiResponse;
import com.music.musicapp.service.MusicPodcastService;
import com.music.musicapp.service.PodcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // ADD KARO
public class MusicPodcastController {
    
    @Autowired
    private MusicPodcastService musicPodcastService;
    
    @Autowired
    private PodcastService podcastService;
    
    // Get combined home page data (music + podcasts)
    @GetMapping("/home")
    public ResponseEntity<ApiResponse> getHome(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            Map<String, Object> homeData = musicPodcastService.getHomeData(userId);
            return ResponseEntity.ok(
                ApiResponse.success("Home data retrieved successfully", homeData)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error getting home data: " + e.getMessage()));
        }
    }
    
    // Combined search (music + podcasts)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchAll(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            Map<String, Object> results = musicPodcastService.searchAll(query, limit, userId);
            return ResponseEntity.ok(
                ApiResponse.success("Search results", results)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error searching: " + e.getMessage()));
        }
    }
    
    // Get personalized recommendations
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse> getRecommendations(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            Map<String, Object> recommendations = musicPodcastService.getUserRecommendations(userId);
            return ResponseEntity.ok(
                ApiResponse.success("Recommendations", recommendations)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error getting recommendations: " + e.getMessage()));
        }
    }
    
    // Get discovery feed (popular music + trending podcasts)
    @GetMapping("/discover")
    public ResponseEntity<ApiResponse> getDiscoverFeed(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            // Create a discovery feed combining music and podcasts
            Map<String, Object> discoverData = Map.of(
                "trendingMusic", musicPodcastService.searchAll("", 5, userId).get("music"),
                "trendingPodcasts", podcastService.getPodcastCategories(),
                "newReleases", Map.of(
                    "music", new String[]{"New Album 1", "New Single 2", "Artist Spotlight"},
                    "podcasts", new String[]{"Latest Episode 1", "New Series 2"}
                ),
                "userBased", userId != null ? "personalized" : "general",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(
                ApiResponse.success("Discovery feed", discoverData)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error getting discovery feed: " + e.getMessage()));
        }
    }
    
    // Health check for combined service
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        try {
            Map<String, Object> healthData = Map.of(
                "service", "MusicPodcastService",
                "status", "operational",
                "timestamp", System.currentTimeMillis(),
                "endpoints", new String[]{"/api/home", "/api/search", "/api/recommendations", "/api/discover"},
                "musicApi", "available",
                "podcastApi", "available"
            );
            
            return ResponseEntity.ok(
                ApiResponse.success("Music-Podcast API is healthy", healthData)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Service health check failed: " + e.getMessage()));
        }
    }
}