package com.music.musicapp.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/check")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "API is working!");
        response.put("timestamp", new Date().toString());
        return response;
    }
    
    @GetMapping("/songs")
    public List<Map<String, Object>> getSongs() {
        List<Map<String, Object>> songs = new ArrayList<>();
        
        // Create sample songs
        String[] titles = {"Blinding Lights", "Save Your Tears", "Levitating", "Stay", "Good 4 U"};
        String[] artists = {"The Weeknd", "The Weeknd", "Dua Lipa", "Kid LAROI & Justin Bieber", "Olivia Rodrigo"};
        
        for (int i = 0; i < titles.length; i++) {
            Map<String, Object> song = new HashMap<>();
            song.put("id", i + 1);
            song.put("title", titles[i]);
            song.put("artist", artists[i]);
            song.put("duration", 180 + (i * 30));
            song.put("genre", "Pop");
            song.put("playCount", 1000 * (i + 1));
            songs.add(song);
        }
        
        return songs;
    }
    
    @GetMapping("/stream/songs")
    public List<Map<String, Object>> getStreamableSongs() {
        return getSongs();
    }
}