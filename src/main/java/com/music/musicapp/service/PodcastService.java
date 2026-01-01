package com.music.musicapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PodcastService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;
    
    @Value("${podcast.listennotes.api.key:}")
    private String listenNotesApiKey;
    
    @Value("${podcast.enabled:true}")
    private boolean podcastEnabled;
    
    public PodcastService(CacheService cacheService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.cacheService = cacheService;
    }
    
    // Search podcasts
    public List<Map<String, Object>> searchPodcasts(String query, int limit) {
        if (!podcastEnabled) {
            return Collections.emptyList();
        }
        
        String cacheKey = "podcast_search_" + query + "_" + limit;
        
        // Check cache
        List<Map<String, Object>> cached = cacheService.getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // Try Listen Notes API first
            if (listenNotesApiKey != null && !listenNotesApiKey.isEmpty()) {
                results = searchListenNotes(query, limit);
            }
            
            // If no results or API not available, use PodcastIndex
            if (results.isEmpty()) {
                results = searchPodcastIndex(query, limit);
            }
            
            // Cache results
            cacheService.saveToCache(cacheKey, results, 3600); // 1 hour
            
        } catch (Exception e) {
            System.err.println("Podcast search error: " + e.getMessage());
        }
        
        return results;
    }
    
    // Search using Listen Notes API
    private List<Map<String, Object>> searchListenNotes(String query, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            String url = "https://listen-api.listennotes.com/api/v2/search";
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("q", query)
                .queryParam("type", "podcast")
                .queryParam("language", "English")
                .queryParam("safe_mode", "1")
                .queryParam("only_in", "title,description")
                .queryParam("sort_by_date", "0")
                .queryParam("len_min", "10")
                .queryParam("len_max", "60");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-ListenAPI-Key", listenNotesApiKey);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode podcasts = root.path("results");
                
                for (JsonNode podcast : podcasts) {
                    if (results.size() >= limit) break;
                    
                    Map<String, Object> podcastInfo = new HashMap<>();
                    podcastInfo.put("id", "listennotes_" + podcast.path("id").asText());
                    podcastInfo.put("title", podcast.path("title_original").asText());
                    podcastInfo.put("description", podcast.path("description_original").asText());
                    podcastInfo.put("publisher", podcast.path("publisher_original").asText());
                    podcastInfo.put("thumbnail", podcast.path("thumbnail").asText());
                    podcastInfo.put("listen_score", podcast.path("listen_score").asDouble());
                    podcastInfo.put("total_episodes", podcast.path("total_episodes").asInt());
                    podcastInfo.put("explicit_content", podcast.path("explicit_content").asBoolean());
                    podcastInfo.put("source", "Listen Notes");
                    
                    // Get genres
                    List<String> genres = new ArrayList<>();
                    JsonNode genreNodes = podcast.path("genre_ids");
                    if (genreNodes.isArray()) {
                        for (JsonNode genreId : genreNodes) {
                            String genre = getGenreName(genreId.asInt());
                            if (genre != null) genres.add(genre);
                        }
                    }
                    podcastInfo.put("genres", genres);
                    
                    results.add(podcastInfo);
                }
            }
        } catch (Exception e) {
            System.err.println("Listen Notes API error: " + e.getMessage());
        }
        
        return results;
    }
    
    // Search using PodcastIndex API
    private List<Map<String, Object>> searchPodcastIndex(String query, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            String url = "https://api.podcastindex.org/api/1.0/search/byterm";
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("q", query)
                .queryParam("max", limit);
            
            // PodcastIndex requires authentication
            long time = System.currentTimeMillis() / 1000;
            String apiHeaderTime = String.valueOf(time);
            // Note: You need to sign the request with API key and secret
            
            ResponseEntity<String> response = restTemplate.getForEntity(
                builder.toUriString(), 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode feeds = root.path("feeds");
                
                for (JsonNode feed : feeds) {
                    Map<String, Object> podcastInfo = new HashMap<>();
                    podcastInfo.put("id", "podcastindex_" + feed.path("id").asText());
                    podcastInfo.put("title", feed.path("title").asText());
                    podcastInfo.put("description", feed.path("description").asText());
                    podcastInfo.put("author", feed.path("author").asText());
                    podcastInfo.put("image", feed.path("image").asText());
                    podcastInfo.put("episodeCount", feed.path("episodeCount").asInt());
                    podcastInfo.put("categories", feed.path("categories").asText());
                    podcastInfo.put("source", "PodcastIndex");
                    
                    results.add(podcastInfo);
                }
            }
        } catch (Exception e) {
            System.err.println("PodcastIndex API error: " + e.getMessage());
        }
        
        return results;
    }
    
    // Get podcast episodes
    public List<Map<String, Object>> getPodcastEpisodes(String podcastId, int limit) {
        String cacheKey = "podcast_episodes_" + podcastId + "_" + limit;
        
        List<Map<String, Object>> cached = cacheService.getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        List<Map<String, Object>> episodes = new ArrayList<>();
        
        try {
            // Mock episodes for demo
            episodes = createMockEpisodes(podcastId, limit);
            
            // Cache results
            cacheService.saveToCache(cacheKey, episodes, 1800); // 30 minutes
            
        } catch (Exception e) {
            System.err.println("Error getting podcast episodes: " + e.getMessage());
        }
        
        return episodes;
    }
    
    // Create mock episodes for demo
    private List<Map<String, Object>> createMockEpisodes(String podcastId, int limit) {
        List<Map<String, Object>> episodes = new ArrayList<>();
        
        String[] episodeTitles = {
            "The Future of AI in Music",
            "Interview with Top Music Producers",
            "History of Jazz Music",
            "How to Start a Podcast",
            "Music Industry Secrets Revealed",
            "Live Concert Special",
            "Artist Spotlight: Rising Stars",
            "Music Production Tips",
            "Behind the Scenes: Album Making",
            "Music Technology Trends 2024"
        };
        
        String[] descriptions = {
            "Exploring how artificial intelligence is changing the music industry.",
            "Exclusive interviews with Grammy-winning producers.",
            "A deep dive into the origins and evolution of jazz music.",
            "Step-by-step guide to launching your own successful podcast.",
            "Insider secrets from music industry veterans.",
            "Recording from our live concert event featuring special guests.",
            "Discovering new and upcoming artists you need to know.",
            "Professional tips for better music production.",
            "What really goes into making a hit album.",
            "The latest trends in music technology and gear."
        };
        
        Random random = new Random();
        for (int i = 0; i < Math.min(limit, episodeTitles.length); i++) {
            Map<String, Object> episode = new HashMap<>();
            episode.put("id", podcastId + "_episode_" + (i + 1));
            episode.put("title", episodeTitles[i]);
            episode.put("description", descriptions[i]);
            episode.put("duration", 1800 + random.nextInt(3600)); // 30-90 minutes
            episode.put("publishedDate", getRandomDate());
            episode.put("audioUrl", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-" + 
                ((i % 10) + 1) + ".mp3");
            episode.put("thumbnail", "https://via.placeholder.com/300/1db954/ffffff?text=Episode+" + (i + 1));
            episode.put("explicit", random.nextBoolean());
            episode.put("playCount", 1000 + random.nextInt(9000));
            episode.put("downloadUrl", "https://example.com/download/" + podcastId + "_" + (i + 1) + ".mp3");
            
            episodes.add(episode);
        }
        
        return episodes;
    }
    
    // Get genre name from ID
    private String getGenreName(int genreId) {
        Map<Integer, String> genres = new HashMap<>();
        genres.put(1, "Arts");
        genres.put(2, "Business");
        genres.put(3, "Comedy");
        genres.put(4, "Education");
        genres.put(5, "Fiction");
        genres.put(6, "Government");
        genres.put(7, "History");
        genres.put(8, "Health & Fitness");
        genres.put(9, "Kids & Family");
        genres.put(10, "Leisure");
        genres.put(11, "Music");
        genres.put(12, "News");
        genres.put(13, "Religion & Spirituality");
        genres.put(14, "Science");
        genres.put(15, "Society & Culture");
        genres.put(16, "Sports");
        genres.put(17, "Technology");
        genres.put(18, "True Crime");
        genres.put(19, "TV & Film");
        
        return genres.get(genreId);
    }
    
    // Get random date for mock data
    private String getRandomDate() {
        Random random = new Random();
        int year = 2023;
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        return String.format("%04d-%02d-%02d", year, month, day);
    }
    
    // Get podcast categories
    public List<Map<String, Object>> getPodcastCategories() {
        return Arrays.asList(
            createCategory("technology", "Technology", "Explore tech podcasts"),
            createCategory("business", "Business", "Business and entrepreneurship"),
            createCategory("comedy", "Comedy", "Funny podcasts"),
            createCategory("education", "Education", "Learn something new"),
            createCategory("music", "Music", "Music industry and artists"),
            createCategory("news", "News", "Current events"),
            createCategory("sports", "Sports", "Sports commentary"),
            createCategory("truecrime", "True Crime", "Crime stories"),
            createCategory("health", "Health & Fitness", "Wellness podcasts"),
            createCategory("fiction", "Fiction", "Storytelling")
        );
    }
    
    private Map<String, Object> createCategory(String id, String name, String description) {
        Map<String, Object> category = new HashMap<>();
        category.put("id", id);
        category.put("name", name);
        category.put("description", description);
        category.put("podcastCount", new Random().nextInt(1000) + 100);
        return category;
    }
    
    // Stream podcast episode
    public Map<String, Object> streamEpisode(String episodeId) {
        Map<String, Object> streamInfo = new HashMap<>();
        
        try {
            // In production, this would handle actual streaming
            // For demo, return mock streaming info
            
            streamInfo.put("episodeId", episodeId);
            streamInfo.put("streamUrl", "https://example.com/stream/" + episodeId);
            streamInfo.put("format", "mp3");
            streamInfo.put("bitrate", "128kbps");
            streamInfo.put("duration", 1800);
            streamInfo.put("supportsSeek", true);
            streamInfo.put("contentType", "audio/mpeg");
            
        } catch (Exception e) {
            streamInfo.put("error", "Streaming not available: " + e.getMessage());
        }
        
        return streamInfo;
    }
    
    // Download podcast episode
    public Map<String, Object> downloadEpisode(String episodeId) {
        Map<String, Object> downloadInfo = new HashMap<>();
        
        try {
            // Mock download info
            downloadInfo.put("episodeId", episodeId);
            downloadInfo.put("downloadUrl", "https://example.com/download/" + episodeId + ".mp3");
            downloadInfo.put("fileSize", "25MB");
            downloadInfo.put("format", "mp3");
            downloadInfo.put("success", true);
            
        } catch (Exception e) {
            downloadInfo.put("success", false);
            downloadInfo.put("error", e.getMessage());
        }
        
        return downloadInfo;
    }
    
    // Subscribe to podcast
    public boolean subscribeToPodcast(String userId, String podcastId) {
        // In production, store in database
        // For demo, just return success
        return true;
    }
    
    // Get subscribed podcasts
    public List<Map<String, Object>> getSubscribedPodcasts(String userId) {
        // Mock subscribed podcasts
        return getPodcastCategories().stream()
            .map(category -> {
                Map<String, Object> podcast = new HashMap<>();
                podcast.put("id", "sub_" + category.get("id"));
                podcast.put("title", "Subscribed to " + category.get("name"));
                podcast.put("category", category.get("name"));
                podcast.put("lastUpdated", getRandomDate());
                return podcast;
            })
            .limit(5)
            .collect(Collectors.toList());
    }
}