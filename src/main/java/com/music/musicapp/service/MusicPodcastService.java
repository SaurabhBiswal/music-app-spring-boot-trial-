package com.music.musicapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MusicPodcastService {
    
    @Autowired
    private MusicApiService musicApiService;
    
    @Autowired
    private PodcastService podcastService;
    
    // Method 1: WITHOUT userId (Controller ke liye compatible)
    public Map<String, Object> searchAll(String query, int limit) {
        return searchAll(query, limit, null);
    }
    
    // Method 2: WITH userId (overloaded version)
    public Map<String, Object> searchAll(String query, int limit, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // Search music
        List<Map<String, Object>> musicResults = musicApiService.searchMusic(query);
        if (musicResults.size() > limit) {
            musicResults = musicResults.subList(0, limit);
        }
        
        // Search podcasts
        List<Map<String, Object>> podcastResults = podcastService.searchPodcasts(query, limit);
        
        result.put("query", query);
        result.put("music", musicResults);
        result.put("podcasts", podcastResults);
        result.put("musicCount", musicResults.size());
        result.put("podcastCount", podcastResults.size());
        result.put("userId", userId);
        result.put("timestamp", new Date());
        result.put("status", "success");
        
        return result;
    }
    
    public Map<String, Object> getUserRecommendations(Long userId) {
        Map<String, Object> recommendations = new HashMap<>();
        
        // Dummy recommendations based on user ID
        if (userId != null) {
            recommendations.put("personalized", true);
            recommendations.put("userId", userId);
            
            // Mock personalized recommendations
            List<Map<String, Object>> recommendedMusic = new ArrayList<>();
            recommendedMusic.add(createItem("rec1", "Your Favorite Genre", "Personalized", "music", "❤️"));
            recommendedMusic.add(createItem("rec2", "Based on History", "AI Picks", "music", "🤖"));
            
            recommendations.put("music", recommendedMusic);
            
            // Podcast recommendations
            List<Map<String, Object>> recommendedPodcasts = podcastService.searchPodcasts("your interests", 3);
            recommendations.put("podcasts", recommendedPodcasts);
            
        } else {
            recommendations.put("personalized", false);
            recommendations.put("music", musicApiService.searchMusic("trending"));
            recommendations.put("podcasts", podcastService.searchPodcasts("popular", 3));
        }
        
        recommendations.put("timestamp", new Date());
        recommendations.put("status", "success");
        
        return recommendations;
    }
    
    // Method 1: WITHOUT userId
    public Map<String, Object> getHomeData() {
        return getHomeData(null);
    }
    
    // Method 2: WITH userId (overloaded version)
    public Map<String, Object> getHomeData(Long userId) {
        Map<String, Object> data = new HashMap<>();
        
        // Featured music
        List<Map<String, Object>> featuredMusic = new ArrayList<>();
        featuredMusic.add(createItem("1", "Top Hits 2024", "Various Artists", "music", "🔥"));
        featuredMusic.add(createItem("2", "Chill Vibes", "Relaxation Mix", "music", "😌"));
        featuredMusic.add(createItem("3", "Workout Energy", "Power Playlist", "music", "💪"));
        data.put("featuredMusic", featuredMusic);
        
        // Featured podcasts
        List<Map<String, Object>> featuredPodcasts = new ArrayList<>();
        featuredPodcasts.add(createItem("p1", "Tech Talks Daily", "Neil C. Hughes", "podcast", "📱"));
        featuredPodcasts.add(createItem("p2", "The Daily", "The New York Times", "podcast", "📰"));
        featuredPodcasts.add(createItem("p3", "Science Vs", "Wendy Zukerman", "podcast", "🔬"));
        data.put("featuredPodcasts", featuredPodcasts);
        
        // Categories
        try {
            data.put("categories", podcastService.getPodcastCategories());
        } catch (Exception e) {
            data.put("categories", getDefaultCategories());
        }
        
        // Add userId if provided
        if (userId != null) {
            data.put("userId", userId);
            data.put("personalized", true);
        } else {
            data.put("personalized", false);
        }
        
        data.put("status", "success");
        data.put("timestamp", new Date());
        data.put("message", "Welcome to Music & Podcast App!");
        
        return data;
    }
    
    private List<Map<String, Object>> getDefaultCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        String[] categoryNames = {"Music", "Technology", "News", "Sports", "Comedy", "Education"};
        
        for (String name : categoryNames) {
            Map<String, Object> category = new HashMap<>();
            category.put("id", name.toLowerCase());
            category.put("name", name);
            category.put("description", name + " content");
            category.put("count", new Random().nextInt(100) + 10);
            categories.add(category);
        }
        
        return categories;
    }
    
    private Map<String, Object> createItem(String id, String title, String creator, String type, String emoji) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("title", title);
        item.put("creator", creator);
        item.put("type", type);
        item.put("emoji", emoji);
        return item;
    }
}
