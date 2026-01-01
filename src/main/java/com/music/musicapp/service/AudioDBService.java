package com.music.musicapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

@Service
public class AudioDBService {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public AudioDBService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://theaudiodb.com/api/v1/json/2")
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public Map<String, Object> getArtistDetails(String artistId) {
        try {
            String response = webClient.get()
                    .uri("/artist.php?i={id}", artistId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode artists = root.path("artists");
            
            if (artists.isArray() && artists.size() > 0) {
                JsonNode artist = artists.get(0);
                Map<String, Object> details = new HashMap<>();
                details.put("idArtist", artist.path("idArtist").asText());
                details.put("strArtist", artist.path("strArtist").asText());
                details.put("strGenre", artist.path("strGenre").asText());
                details.put("strBiographyEN", artist.path("strBiographyEN").asText());
                details.put("strCountry", artist.path("strCountry").asText());
                details.put("strArtistThumb", artist.path("strArtistThumb").asText());
                details.put("strArtistLogo", artist.path("strArtistLogo").asText());
                details.put("strArtistFanart", artist.path("strArtistFanart").asText());
                
                return details;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return Collections.emptyMap();
    }
    
    public List<Map<String, Object>> searchTrack(String artist, String track) {
        try {
            String response = webClient.get()
                    .uri("/searchtrack.php?s={artist}&t={track}", artist, track)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode tracks = root.path("track");
            
            List<Map<String, Object>> results = new ArrayList<>();
            if (tracks.isArray()) {
                for (JsonNode trackNode : tracks) {
                    Map<String, Object> trackData = new HashMap<>();
                    trackData.put("idTrack", trackNode.path("idTrack").asText());
                    trackData.put("strTrack", trackNode.path("strTrack").asText());
                    trackData.put("strArtist", trackNode.path("strArtist").asText());
                    trackData.put("strAlbum", trackNode.path("strAlbum").asText());
                    trackData.put("strGenre", trackNode.path("strGenre").asText());
                    trackData.put("intDuration", trackNode.path("intDuration").asText());
                    trackData.put("strTrackThumb", trackNode.path("strTrackThumb").asText());
                    
                    results.add(trackData);
                }
            }
            
            return results;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}