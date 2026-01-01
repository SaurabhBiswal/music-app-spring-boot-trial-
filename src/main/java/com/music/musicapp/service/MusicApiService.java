package com.music.musicapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class MusicApiService {
    
    public List<Map<String, Object>> searchMusic(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // Try MusicBrainz API first
            results = searchMusicBrainz(query);
            
            // If no results, use mock data
            if (results.isEmpty()) {
                results = getMockMusicData(query);
            }
            
        } catch (Exception e) {
            // Fallback to mock data
            results = getMockMusicData(query);
        }
        
        return results;
    }
    
    private List<Map<String, Object>> searchMusicBrainz(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            String url = "https://musicbrainz.org/ws/2/recording/?query=" + 
                        query.replace(" ", "+") + "&fmt=json&limit=5";
            
            RestTemplate restTemplate = new RestTemplate();
            
            // MusicBrainz requires User-Agent
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "MusicApp/1.0");
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            String response = restTemplate.exchange(
                url, 
                org.springframework.http.HttpMethod.GET, 
                entity, 
                String.class
            ).getBody();
            
            if (response != null) {
                results = parseMusicBrainzResponse(response);
            }
            
        } catch (Exception e) {
            // If API fails, return empty list - will use mock data
        }
        
        return results;
    }
    
    private List<Map<String, Object>> parseMusicBrainzResponse(String json) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            String cleaned = json.replace("\n", "").replace("\r", "");
            
            if (cleaned.contains("\"recordings\":[")) {
                String recordingsPart = cleaned.split("\"recordings\":\\[")[1].split("\\]")[0];
                String[] recordings = recordingsPart.split("\\},\\{");
                
                for (String recording : recordings) {
                    Map<String, Object> track = new HashMap<>();
                    
                    // Extract title
                    if (recording.contains("\"title\":\"")) {
                        String title = recording.split("\"title\":\"")[1].split("\"")[0];
                        track.put("title", title);
                    } else {
                        track.put("title", "Unknown Track");
                    }
                    
                    // Extract artist
                    String artist = "Unknown Artist";
                    if (recording.contains("\"artist-credit\"")) {
                        String artistPart = recording.split("\"artist-credit\":\\[")[1].split("\\]")[0];
                        if (artistPart.contains("\"name\":\"")) {
                            artist = artistPart.split("\"name\":\"")[1].split("\"")[0];
                        }
                    }
                    track.put("artist", artist);
                    
                    // Extract duration
                    int duration = 180;
                    if (recording.contains("\"length\":")) {
                        try {
                            String lengthStr = recording.split("\"length\":")[1].split(",")[0].trim();
                            duration = Integer.parseInt(lengthStr) / 1000;
                        } catch (Exception e) {
                            duration = 180;
                        }
                    }
                    track.put("duration", duration);
                    
                    track.put("id", "music_" + System.currentTimeMillis() + "_" + results.size());
                    track.put("source", "MusicBrainz");
                    track.put("type", "music");
                    
                    results.add(track);
                }
            }
        } catch (Exception e) {
            // If parsing fails, return empty
        }
        
        return results;
    }
    
    private List<Map<String, Object>> getMockMusicData(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Popular music tracks
        String[][] tracks = {
            {"Imagine", "John Lennon", "180"},
            {"Bohemian Rhapsody", "Queen", "354"},
            {"Hotel California", "Eagles", "391"},
            {"Stairway to Heaven", "Led Zeppelin", "482"},
            {"Billie Jean", "Michael Jackson", "294"},
            {"Like a Rolling Stone", "Bob Dylan", "369"},
            {"Smells Like Teen Spirit", "Nirvana", "301"},
            {"What'\''s Going On", "Marvin Gaye", "233"},
            {"Good Vibrations", "The Beach Boys", "216"},
            {"Johnny B. Goode", "Chuck Berry", "161"}
        };
        
        // Filter by query if provided
        for (String[] track : tracks) {
            if (query.isEmpty() || 
                track[0].toLowerCase().contains(query.toLowerCase()) || 
                track[1].toLowerCase().contains(query.toLowerCase())) {
                
                Map<String, Object> trackData = new HashMap<>();
                trackData.put("id", "mock_" + track[0].replace(" ", "_").toLowerCase());
                trackData.put("title", track[0]);
                trackData.put("artist", track[1]);
                trackData.put("duration", Integer.parseInt(track[2]));
                trackData.put("source", "Music Library");
                trackData.put("type", "music");
                
                results.add(trackData);
            }
        }
        
        // If no matches, return some default tracks
        if (results.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                Map<String, Object> trackData = new HashMap<>();
                trackData.put("id", "track_" + (i + 1));
                trackData.put("title", query + " - Song " + (i + 1));
                trackData.put("artist", "Artist " + (char)('A' + i));
                trackData.put("duration", 180 + (i * 30));
                trackData.put("source", "Music Library");
                trackData.put("type", "music");
                results.add(trackData);
            }
        }
        
        return results;
    }
}