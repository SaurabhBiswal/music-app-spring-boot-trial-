package com.music.musicapp.controller;

import com.music.musicapp.dto.ApiResponse;
import com.music.musicapp.dto.PlaylistDTO;
import com.music.musicapp.dto.SongDTO;
import com.music.musicapp.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
@CrossOrigin(origins = "*") 
public class PlaylistController {
    
    @Autowired
    private PlaylistService playlistService;

    // --- 1. PLAYLIST MANAGEMENT (Create/Delete/Rename) ---

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPlaylist(@RequestParam String name, @RequestParam(required = false) Long userId) {
        try {
            PlaylistDTO playlist = playlistService.createPlaylist(name, "My Playlist", true, userId);
            return ResponseEntity.ok(ApiResponse.success("Playlist created successfully", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePlaylist(@PathVariable Long id) {
        try {
            playlistService.deletePlaylist(id);
            return ResponseEntity.ok(ApiResponse.success("Playlist deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> renamePlaylist(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            // ✅ FIXED: Direct repository use karo
            PlaylistDTO playlist = playlistService.renamePlaylist(id, name, userId);
            return ResponseEntity.ok(ApiResponse.success("Playlist renamed successfully", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    // --- 2. RETRIEVAL (Get Playlists) ---

    @GetMapping
    public ResponseEntity<ApiResponse> getAllPublicPlaylists() {
        try {
            List<PlaylistDTO> playlists = playlistService.getAllPublicPlaylists();
            return ResponseEntity.ok(ApiResponse.success("Public playlists retrieved", playlists));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getPlaylistByUserId(@PathVariable Long userId) {
        try {
            List<PlaylistDTO> playlists = playlistService.getPlaylistsByUserId(userId); 
            if (playlists != null && !playlists.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("User Library retrieved", playlists.get(0)));
            }
            return ResponseEntity.ok(ApiResponse.success("No playlists found for this user", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<ApiResponse> getAllUserPlaylists(@PathVariable Long userId) {
        try {
            List<PlaylistDTO> playlists = playlistService.getPlaylistsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success("All user playlists retrieved", playlists));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPlaylistById(@PathVariable Long id) {
        try {
            PlaylistDTO playlist = playlistService.getPlaylistById(id);
            return ResponseEntity.ok(ApiResponse.success("Playlist retrieved", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/share")
    public ResponseEntity<ApiResponse> getPlaylistShareInfo(@PathVariable Long id) {
        try {
            PlaylistDTO playlist = playlistService.getPlaylistById(id);
            
            Map<String, Object> shareInfo = new HashMap<>();
            shareInfo.put("id", playlist.getId());
            shareInfo.put("name", playlist.getName());
            shareInfo.put("description", playlist.getDescription());
            shareInfo.put("isPublic", playlist.isPublic());
            shareInfo.put("songCount", playlist.getSongs() != null ? playlist.getSongs().size() : 0);
            
            // ✅ FIXED: getUsername() use karo, getUser() nahi
            shareInfo.put("createdBy", playlist.getUsername() != null ? playlist.getUsername() : "Anonymous");
            
            shareInfo.put("shareUrl", "http://localhost:8080/#/playlist/" + playlist.getId());
            shareInfo.put("qrCode", "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + 
                "http://localhost:8080/#/playlist/" + playlist.getId());
            shareInfo.put("shareable", playlist.isPublic());
            
            return ResponseEntity.ok(ApiResponse.success("Share info", shareInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // --- 3. SONG MANAGEMENT (Add/Remove) ---

    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<ApiResponse> addSongToPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        try {
            PlaylistDTO playlist = playlistService.addSongToPlaylist(playlistId, songId);
            return ResponseEntity.ok(ApiResponse.success("Song added to library", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{playlistId}/songs")
    public ResponseEntity<ApiResponse> addExternalSongToPlaylist(@PathVariable Long playlistId, @RequestBody SongDTO songDTO) {
        try {
            PlaylistDTO playlist = playlistService.addExternalSongToPlaylist(playlistId, songDTO);
            return ResponseEntity.ok(ApiResponse.success("YouTube song added to library!", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<ApiResponse> removeSongFromPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        try {
            PlaylistDTO playlist = playlistService.removeSongFromPlaylist(playlistId, songId);
            return ResponseEntity.ok(ApiResponse.success("Song removed", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // --- 4. UTILS (Search & Health) ---

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchPlaylistsByName(@RequestParam String name) {
        try {
            List<PlaylistDTO> playlists = playlistService.searchPlaylistsByName(name);
            return ResponseEntity.ok(ApiResponse.success("Search results", playlists));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "Playlist Service");
            health.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(ApiResponse.success("Playlist API is healthy", health));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Playlist API is down: " + e.getMessage()));
        }
    }
}