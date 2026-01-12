package com.music.musicapp.controller;

import com.music.musicapp.dto.ApiResponse;
import com.music.musicapp.model.Song;
import com.music.musicapp.model.User;
import com.music.musicapp.repository.UserRepository;
import com.music.musicapp.repository.SongRepository;
import com.music.musicapp.repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic stats
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSongs", songRepository.count());
        stats.put("totalPlaylists", playlistRepository.count());
        
        // Recent users (last 5)
        List<User> recentUsers = userRepository.findTop5ByOrderByIdDesc();
        stats.put("recentUsers", recentUsers);
        
        // Popular songs (top 10 by play count)
        List<Song> popularSongs = songRepository.findTop10ByOrderByPlayCountDesc();
        stats.put("popularSongs", popularSongs);
        
        return ResponseEntity.ok(ApiResponse.success("Admin stats", stats));
    }
}