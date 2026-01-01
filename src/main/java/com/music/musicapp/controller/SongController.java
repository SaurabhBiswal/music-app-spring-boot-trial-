package com.music.musicapp.controller;

import com.music.musicapp.dto.ApiResponse;
import com.music.musicapp.dto.SongDTO;
import com.music.musicapp.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {
    
    @Autowired
    private SongService songService;
    
    // Get all songs
    @GetMapping
    public ResponseEntity<ApiResponse> getAllSongs() {
        List<SongDTO> songs = songService.getAllSongs();
        return ResponseEntity.ok(
            ApiResponse.success("Songs retrieved successfully", songs)
        );
    }
    
    // Get song by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getSongById(@PathVariable Long id) {
        try {
            SongDTO song = songService.getSongById(id);
            return ResponseEntity.ok(
                ApiResponse.success("Song found", song)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Search songs by title
    @GetMapping("/search/title")
    public ResponseEntity<ApiResponse> searchByTitle(@RequestParam String title) {
        List<SongDTO> songs = songService.searchByTitle(title);
        return ResponseEntity.ok(
            ApiResponse.success("Search results for: " + title, songs)
        );
    }
    
    // Search songs by artist
    @GetMapping("/search/artist")
    public ResponseEntity<ApiResponse> searchByArtist(@RequestParam String artist) {
        List<SongDTO> songs = songService.searchByArtist(artist);
        return ResponseEntity.ok(
            ApiResponse.success("Search results for artist: " + artist, songs)
        );
    }
    
    // Get songs by genre
    @GetMapping("/genre/{genre}")
    public ResponseEntity<ApiResponse> getSongsByGenre(@PathVariable String genre) {
        List<SongDTO> songs = songService.getSongsByGenre(genre);
        return ResponseEntity.ok(
            ApiResponse.success("Songs in genre: " + genre, songs)
        );
    }
    
    // Get recently added songs
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse> getRecentSongs(
        @RequestParam(defaultValue = "5") int limit
    ) {
        List<SongDTO> songs = songService.getRecentSongs(limit);
        return ResponseEntity.ok(
            ApiResponse.success("Recent songs (limit: " + limit + ")", songs)
        );
    }
    
    // NEW ENDPOINT: Update song audio file path
    @PutMapping("/{id}/audio")
    public ResponseEntity<ApiResponse> updateSongAudio(
        @PathVariable Long id,
        @RequestParam("filename") String filename) {
        try {
            SongDTO updatedSong = songService.updateSongFilePath(id, filename);
            return ResponseEntity.ok(
                ApiResponse.success("Audio file updated for song", updatedSong)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // NEW ENDPOINT: Get songs with audio files
    @GetMapping("/with-audio")
    public ResponseEntity<ApiResponse> getSongsWithAudio() {
        List<SongDTO> songs = songService.getSongsWithAudio();
        return ResponseEntity.ok(
            ApiResponse.success("Songs with audio files", songs)
        );
    }
    
    // Health check
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Song API is working!");
    }
    
    // Homepage endpoint - get featured songs
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse> getFeaturedSongs() {
        // Get 5 recent songs as featured
        List<SongDTO> songs = songService.getRecentSongs(5);
        return ResponseEntity.ok(
            ApiResponse.success("Featured songs", songs)
        );
    }
}