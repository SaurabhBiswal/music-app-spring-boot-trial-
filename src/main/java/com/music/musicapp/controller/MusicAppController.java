package com.music.musicapp.controller;

import com.music.musicapp.service.MusicPodcastService;
import com.music.musicapp.service.PodcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/music")
@CrossOrigin(origins = "*")
public class MusicAppController {
    
    @Autowired
    private MusicPodcastService musicPodcastService;
    
    @Autowired
    private PodcastService podcastService;
    
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String q) {
        return musicPodcastService.searchAll(q, 10);
    }
    
    @GetMapping("/search/music")
    public Map<String, Object> searchMusic(@RequestParam String q) {
        Map<String, Object> response = new HashMap<>();
        response.put("query", q);
        response.put("music", musicPodcastService.searchAll(q, 10).get("music"));
        response.put("status", "success");
        return response;
    }
    
    @GetMapping("/search/podcasts")
    public Map<String, Object> searchPodcasts(@RequestParam String q) {
        Map<String, Object> response = new HashMap<>();
        response.put("query", q);
        response.put("podcasts", podcastService.searchPodcasts(q, 10));
        response.put("status", "success");
        return response;
    }
    
    @GetMapping("/home")
    public Map<String, Object> getHomeData() {
        return musicPodcastService.getHomeData();
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("app", "Music & Podcast App");
        response.put("version", "1.0.0");
        response.put("date", new Date().toString());
        return response;
    }
    
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "🎉 App is working! Happy New Year 2025! 🎊");
        response.put("timestamp", new Date());
        response.put("endpoints", Arrays.asList(
            "/api/health - Health check",
            "/api/search?q=rock - Search music & podcasts",
            "/api/home - Homepage data"
        ));
        return response;
    }
}