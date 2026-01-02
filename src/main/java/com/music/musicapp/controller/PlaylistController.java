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
@CrossOrigin(origins = "*") 
public class PlaylistController {
    
    @Autowired
    private PlaylistService playlistService;

    // --- 1. PLAYLIST MANAGEMENT (Create/Delete) ---

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPlaylist(@RequestParam String name, @RequestParam(required = false) Long userId) {
        try {
            // "My Playlist" default description hai. Agar userId hai toh link ho jayega.
            PlaylistDTO playlist = playlistService.createPlaylist(name, "My Playlist", true, userId);
            return ResponseEntity.ok(ApiResponse.success("Playlist created successfully", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.ok(ApiResponse.success("Playlist deleted", null));
    }

    // --- 2. RETRIEVAL (Get Playlists) ---

    // Get all public playlists for Explore page
    @GetMapping
    public ResponseEntity<ApiResponse> getAllPublicPlaylists() {
        try {
            List<PlaylistDTO> playlists = playlistService.getAllPublicPlaylists();
            return ResponseEntity.ok(ApiResponse.success("Public playlists retrieved", playlists));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // IMPORTANT: Get specific user's playlist (Saurabh vs Admin Fix)
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getPlaylistByUserId(@PathVariable Long userId) {
        try {
            List<PlaylistDTO> playlists = playlistService.getPlaylistsByUserId(userId); 
            if (playlists != null && !playlists.isEmpty()) {
                // Hum user ki pehli playlist (Default Library) return kar rahe hain
                return ResponseEntity.ok(ApiResponse.success("User Library retrieved", playlists.get(0)));
            }
            return ResponseEntity.ok(ApiResponse.success("No playlists found for this user", null));
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

    // --- 3. SONG MANAGEMENT (Add/Remove) ---

    // Add Internal Song (Database mein jo pehle se hai)
    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<ApiResponse> addSongToPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        try {
            PlaylistDTO playlist = playlistService.addSongToPlaylist(playlistId, songId);
            return ResponseEntity.ok(ApiResponse.success("Song added to library", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Add External Song (YouTube link wala direct)
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

    // --- 4. UTILS (Search) ---

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchPlaylistsByName(@RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.success("Search results", playlistService.searchPlaylistsByName(name)));
    }
}