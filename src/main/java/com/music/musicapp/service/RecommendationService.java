package com.music.musicapp.service;

import com.music.musicapp.model.PlayHistory;
import com.music.musicapp.model.Rating;
import com.music.musicapp.model.Song;
import com.music.musicapp.model.User;
import com.music.musicapp.repository.PlayHistoryRepository;
import com.music.musicapp.repository.RatingRepository;
import com.music.musicapp.repository.SongRepository;
import com.music.musicapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    
    @Autowired
    private PlayHistoryRepository playHistoryRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private SongRepository songRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Collaborative filtering for recommendations
    public List<Map<String, Object>> getPersonalizedRecommendations(Long userId, int limit) {
        // For now, return trending recommendations
        return getTrendingRecommendations(limit);
    }
    
    // Get trending recommendations - SIMPLIFIED
    private List<Map<String, Object>> getTrendingRecommendations(int limit) {
        // Get all songs and sort by play count manually
        List<Song> allSongs = songRepository.findAll();
        
        // Sort by play count if available
        allSongs.sort((a, b) -> {
            Integer countA = a.getPlayCount() != null ? a.getPlayCount() : 0;
            Integer countB = b.getPlayCount() != null ? b.getPlayCount() : 0;
            return countB.compareTo(countA);
        });
        
        return allSongs.stream()
            .limit(limit)
            .map(this::convertSongToMap)
            .peek(track -> track.put("recommendationReason", "Trending this week"))
            .collect(Collectors.toList());
    }
    
    // Get recommendations based on user preferences
    private List<Map<String, Object>> getRecommendationsByPreferences(
            List<String> topGenres, List<String> topArtists, int limit, Long excludeUserId) {
        return new ArrayList<>(); // Simplified for now
    }
    
    // Find similar users (simplified implementation)
    private List<Long> findSimilarUsers(Long userId, List<String> topGenres, List<String> topArtists) {
        return new ArrayList<>(); // Simplified for now
    }
    
    // Get top items from map
    private List<String> getTopItems(Map<String, Integer> itemMap, int limit) {
        return itemMap.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    // Check if user has listened to a track
    private boolean hasUserListenedTo(Long userId, Long songId) {
        if (songId == null) return false;
        
        List<PlayHistory> userHistory = playHistoryRepository.findByUserId(userId);
        return userHistory.stream()
            .anyMatch(history -> history.getSongId() != null && history.getSongId().equals(songId));
    }
    
    // Convert Song entity to map
    private Map<String, Object> convertSongToMap(Song song) {
        Map<String, Object> track = new HashMap<>();
        track.put("id", String.valueOf(song.getId()));
        track.put("title", song.getTitle());
        track.put("artist", song.getArtist());
        track.put("album", song.getAlbum());
        track.put("duration", song.getDuration());
        track.put("genre", song.getGenre());
        track.put("albumArt", song.getAlbumArtUrl());
        track.put("year", song.getReleaseYear());
        
        // Calculate average rating
        Double avgRating = null;
        if (song.getId() != null) {
            avgRating = ratingRepository.findAverageRatingBySongId(song.getId());
        }
        track.put("averageRating", avgRating != null ? avgRating : 4.0);
        
        // Get play count
        track.put("playCount", song.getPlayCount() != null ? song.getPlayCount() : 0);
        
        return track;
    }
    
    // Record user play history - FIXED
    public void recordPlay(Long userId, Long songId, int playDuration) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return;
        
        PlayHistory playHistory = new PlayHistory();
        // Check what fields your PlayHistory entity actually has
        // Based on your earlier code, it should have:
        playHistory.setUser(userOpt.get());
        playHistory.setSongId(songId);
        playHistory.setPlayDurationSeconds(playDuration);
        playHistory.setPlayedAt(LocalDateTime.now());
        
        playHistoryRepository.save(playHistory);
        
        // Increment song play count
        Optional<Song> songOpt = songRepository.findById(songId);
        songOpt.ifPresent(song -> {
            song.setPlayCount((song.getPlayCount() == null ? 0 : song.getPlayCount()) + 1);
            songRepository.save(song);
        });
    }
    
    // Add user rating
    public void addRating(Long userId, String trackId, double rating, String review) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return;
        
        Rating userRating = new Rating();
        userRating.setUser(userOpt.get());
        userRating.setTrackId(trackId);
        userRating.setRatingValue((int) rating);
        userRating.setComment(review);
        
        ratingRepository.save(userRating);
    }
    
    // Get user rating for a track
    public Map<String, Object> getUserRating(Long userId, String trackId) {
        Optional<Rating> ratingOpt = ratingRepository.findByUserIdAndTrackId(userId, trackId);
        
        if (ratingOpt.isPresent()) {
            Rating rating = ratingOpt.get();
            Map<String, Object> ratingMap = new HashMap<>();
            ratingMap.put("rating", rating.getRatingValue());
            ratingMap.put("review", rating.getComment());
            ratingMap.put("ratedAt", rating.getRatedAt());
            return ratingMap;
        }
        
        return null;
    }
    
    // Generate recommendation explanation
    public String getRecommendationExplanation(Long userId) {
        return "Based on popular tracks";
    }
}