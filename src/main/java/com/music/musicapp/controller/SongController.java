package com.music.musicapp.controller;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import com.music.musicapp.dto.ApiResponse;
import com.music.musicapp.dto.SongDTO;
import com.music.musicapp.model.Song;
import com.music.musicapp.repository.SongRepository;
import com.music.musicapp.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "*")
public class SongController {
    
    @Autowired
    private SongService songService;
    @Autowired
private SongRepository songRepository;
    // FIX: Upload Endpoint jo missing tha
 @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadSong(
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam("file") MultipartFile file) {
        try {
            SongDTO savedSong = songService.saveSong(title, artist, "Unknown", file);
            return ResponseEntity.ok(ApiResponse.success("Song uploaded", savedSong));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    // Get all songs
    @GetMapping("/all")
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
    // ==========================================
    // STREAMING ENDPOINT (Gaana Bajane Ke Liye)
    // ==========================================
    @GetMapping("/play/{filename}")
    public ResponseEntity<Resource> playAudio(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/audio").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg"))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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
    @GetMapping("/recommendations/{songId}")
public ResponseEntity<ApiResponse> getRecommendations(@PathVariable Long songId) {
    Optional<Song> currentSongOpt = songRepository.findById(songId);
    
    if (currentSongOpt.isEmpty()) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Song not found"));
    }
    
    Song currentSong = currentSongOpt.get();
    
    // ✅ FIX: Pehle recommendations ko initialize karo
    List<Song> recommendations = new ArrayList<>();
    
    // Same artist ke songs
    if (currentSong.getArtist() != null && !currentSong.getArtist().trim().isEmpty()) {
        List<Song> sameArtistSongs = songRepository
            .findByArtistContainingIgnoreCase(currentSong.getArtist());
        
        // ✅ FIX: New variable mein store karo
        List<Song> artistRecommendations = sameArtistSongs.stream()
            .filter(song -> !song.getId().equals(songId))
            .limit(3)
            .collect(Collectors.toList());
        
        recommendations.addAll(artistRecommendations);
    }
    
    // Agar kam recommendations hain, toh same genre ke songs add karo
    if (recommendations.size() < 3 && currentSong.getGenre() != null && !currentSong.getGenre().trim().isEmpty()) {
        List<Song> sameGenreSongs = songRepository
            .findByGenreContainingIgnoreCase(currentSong.getGenre());
        
        List<Song> genreRecommendations = sameGenreSongs.stream()
            .filter(song -> !song.getId().equals(songId))
            .filter(song -> recommendations.stream().noneMatch(rec -> rec.getId().equals(song.getId())))
            .limit(3 - recommendations.size())
            .collect(Collectors.toList());
        
        recommendations.addAll(genreRecommendations);
    }
    
    return ResponseEntity.ok(ApiResponse.success("Recommendations", recommendations));
}
    @GetMapping("/search/external")
public ResponseEntity<ApiResponse> searchExternal(@RequestParam String query) {
    try {
        List<SongDTO> songs = songService.searchExternalSongs(query);
        return ResponseEntity.ok(ApiResponse.success("External songs found", songs));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
}
// SongController.java

@PostMapping("/add-external")
public ResponseEntity<ApiResponse> addExternalSong(@RequestBody SongDTO songDTO) {
    try {
        // Check if song already exists using the method we added in Repository
        Optional<Song> existing = songRepository.findByAudioUrl(songDTO.getAudioUrl());
        
        if (existing.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("Song already exists", existing.get()));
        }

        Song song = new Song();
        song.setTitle(songDTO.getTitle());
        song.setArtist(songDTO.getArtist());
        song.setAudioUrl(songDTO.getAudioUrl());
        song.setAlbumArtUrl(songDTO.getAlbumArtUrl());
        song.setUploadedAt(LocalDateTime.now()); // Optional but good for stats

        Song saved = songRepository.save(song);
        return ResponseEntity.ok(ApiResponse.success("Song synced to DB", saved));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Sync failed: " + e.getMessage()));
    }
}
}