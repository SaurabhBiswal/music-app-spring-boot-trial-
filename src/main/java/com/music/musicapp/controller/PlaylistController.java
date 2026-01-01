package com.music.musicapp.controller;

import com.music.musicapp.dto.ApiResponse;
import com.music.musicapp.dto.PlaylistDTO;
import com.music.musicapp.dto.SongDTO;
import com.music.musicapp.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {
    
    @Autowired
    private PlaylistService playlistService;
    
    // Get all public playlists
    @GetMapping
    public ResponseEntity<ApiResponse> getAllPublicPlaylists() {
        try {
            List<PlaylistDTO> playlists = playlistService.getAllPublicPlaylists();
            return ResponseEntity.ok(
                ApiResponse.success("Public playlists retrieved successfully", playlists)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error getting playlists: " + e.getMessage()));
        }
    }
    
    // Get playlist by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPlaylistById(@PathVariable Long id) {
        try {
            PlaylistDTO playlist = playlistService.getPlaylistById(id);
            return ResponseEntity.ok(
                ApiResponse.success("Playlist retrieved successfully", playlist)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Create a new playlist
    @PostMapping
    public ResponseEntity<ApiResponse> createPlaylist(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Boolean isPublic = (Boolean) request.get("isPublic");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Playlist name is required"));
            }
            
            if (isPublic == null) {
                isPublic = true; // Default to public
            }
            
            PlaylistDTO playlist = playlistService.createPlaylist(name, description, isPublic, userId);
            return ResponseEntity.ok(
                ApiResponse.success("Playlist created successfully", playlist)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error creating playlist: " + e.getMessage()));
        }
    }
    
    // Update playlist
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updatePlaylist(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Boolean isPublic = (Boolean) request.get("isPublic");
            
            PlaylistDTO playlist = playlistService.updatePlaylist(id, name, description, isPublic);
            return ResponseEntity.ok(
                ApiResponse.success("Playlist updated successfully", playlist)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Delete playlist
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePlaylist(@PathVariable Long id) {
        try {
            boolean deleted = playlistService.deletePlaylist(id);
            if (deleted) {
                return ResponseEntity.ok(
                    ApiResponse.success("Playlist deleted successfully", null)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Playlist not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error deleting playlist: " + e.getMessage()));
        }
    }
    
    // Add song to playlist
    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<ApiResponse> addSongToPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        try {
            PlaylistDTO playlist = playlistService.addSongToPlaylist(playlistId, songId);
            return ResponseEntity.ok(
                ApiResponse.success("Song added to playlist successfully", playlist)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Remove song from playlist
    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<ApiResponse> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        try {
            PlaylistDTO playlist = playlistService.removeSongFromPlaylist(playlistId, songId);
            return ResponseEntity.ok(
                ApiResponse.success("Song removed from playlist successfully", playlist)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get songs in a playlist
    @GetMapping("/{id}/songs")
    public ResponseEntity<ApiResponse> getSongsInPlaylist(@PathVariable Long id) {
        try {
            List<SongDTO> songs = playlistService.getSongsInPlaylist(id);
            return ResponseEntity.ok(
                ApiResponse.success("Songs in playlist retrieved successfully", songs)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Search playlists by name
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchPlaylistsByName(@RequestParam String name) {
        try {
            List<PlaylistDTO> playlists = playlistService.searchPlaylistsByName(name);
            return ResponseEntity.ok(
                ApiResponse.success("Playlists search results", playlists)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error searching playlists: " + e.getMessage()));
        }
    }
    
    // Get playlists containing a specific song
    @GetMapping("/containing-song/{songId}")
    public ResponseEntity<ApiResponse> getPlaylistsContainingSong(@PathVariable Long songId) {
        try {
            List<PlaylistDTO> playlists = playlistService.getPlaylistsContainingSong(songId);
            return ResponseEntity.ok(
                ApiResponse.success("Playlists containing the song", playlists)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error getting playlists: " + e.getMessage()));
        }
    }
    
    // Health check
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Playlist API is working!", Map.of("status", "healthy", "timestamp", System.currentTimeMillis()))
        );
    }
}