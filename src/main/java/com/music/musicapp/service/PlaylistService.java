package com.music.musicapp.service;

import com.music.musicapp.dto.PlaylistDTO;
import com.music.musicapp.dto.SongDTO;
import com.music.musicapp.model.Playlist;
import com.music.musicapp.model.Song;
import com.music.musicapp.model.User;
import com.music.musicapp.repository.PlaylistRepository;
import com.music.musicapp.repository.SongRepository;
import com.music.musicapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlaylistService {
    
    @Autowired
    private PlaylistRepository playlistRepository;
    
    @Autowired
    private SongRepository songRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SongService songService;
    
    // Get all public playlists
    public List<PlaylistDTO> getAllPublicPlaylists() {
        return playlistRepository.findByIsPublicTrue().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get playlist by ID
    public PlaylistDTO getPlaylistById(Long id) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(id);
        
        if (playlistOpt.isEmpty()) {
            throw new RuntimeException("Playlist not found with ID: " + id);
        }
        
        Playlist playlist = playlistOpt.get();
        
        // Check if playlist is public
        if (!playlist.isPublic()) {
            throw new RuntimeException("Playlist is private");
        }
        
        return convertToDTO(playlist);
    }
    
    // Create a new playlist
    @Transactional
    public PlaylistDTO createPlaylist(String name, String description, boolean isPublic, Long userId) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setDescription(description);
        playlist.setPublic(isPublic);
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setUpdatedAt(LocalDateTime.now());
        
        // Set owner if user exists
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            userOpt.ifPresent(playlist::setUser);
        }
        
        Playlist savedPlaylist = playlistRepository.save(playlist);
        return convertToDTO(savedPlaylist);
    }
    
    // Update playlist
    @Transactional
    public PlaylistDTO updatePlaylist(Long id, String name, String description, Boolean isPublic) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(id);
        
        if (playlistOpt.isEmpty()) {
            throw new RuntimeException("Playlist not found with ID: " + id);
        }
        
        Playlist playlist = playlistOpt.get();
        
        if (name != null) {
            playlist.setName(name);
        }
        
        if (description != null) {
            playlist.setDescription(description);
        }
        
        if (isPublic != null) {
            playlist.setPublic(isPublic);
        }
        
        playlist.setUpdatedAt(LocalDateTime.now());
        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return convertToDTO(updatedPlaylist);
    }
    
    // Delete playlist
    @Transactional
    public boolean deletePlaylist(Long id) {
        if (playlistRepository.existsById(id)) {
            playlistRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Add song to playlist
    @Transactional
    public PlaylistDTO addSongToPlaylist(Long playlistId, Long songId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        Optional<Song> songOpt = songRepository.findById(songId);
        
        if (playlistOpt.isEmpty()) {
            throw new RuntimeException("Playlist not found with ID: " + playlistId);
        }
        
        if (songOpt.isEmpty()) {
            throw new RuntimeException("Song not found with ID: " + songId);
        }
        
        Playlist playlist = playlistOpt.get();
        Song song = songOpt.get();
        
        // Initialize songs list if null
        if (playlist.getSongs() == null) {
            playlist.setSongs(new ArrayList<>());
        }
        
        // Check if song already in playlist
        if (playlist.getSongs().contains(song)) {
            throw new RuntimeException("Song already in playlist");
        }
        
        playlist.getSongs().add(song);
        playlist.setUpdatedAt(LocalDateTime.now());
        Playlist updatedPlaylist = playlistRepository.save(playlist);
        
        return convertToDTO(updatedPlaylist);
    }
    
    // Remove song from playlist
    @Transactional
    public PlaylistDTO removeSongFromPlaylist(Long playlistId, Long songId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        Optional<Song> songOpt = songRepository.findById(songId);
        
        if (playlistOpt.isEmpty()) {
            throw new RuntimeException("Playlist not found with ID: " + playlistId);
        }
        
        if (songOpt.isEmpty()) {
            throw new RuntimeException("Song not found with ID: " + songId);
        }
        
        Playlist playlist = playlistOpt.get();
        Song song = songOpt.get();
        
        // Check if songs list exists and contains the song
        if (playlist.getSongs() == null || !playlist.getSongs().contains(song)) {
            throw new RuntimeException("Song not in playlist");
        }
        
        playlist.getSongs().remove(song);
        playlist.setUpdatedAt(LocalDateTime.now());
        Playlist updatedPlaylist = playlistRepository.save(playlist);
        
        return convertToDTO(updatedPlaylist);
    }
    
    // Search playlists by name
    public List<PlaylistDTO> searchPlaylistsByName(String name) {
        return playlistRepository.findByIsPublicTrueAndNameContainingIgnoreCase(name).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get playlists containing a specific song
    public List<PlaylistDTO> getPlaylistsContainingSong(Long songId) {
        // Fixed: Use the repository method instead of manual filtering
        return playlistRepository.findPlaylistsContainingSong(songId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get songs in a playlist
    public List<SongDTO> getSongsInPlaylist(Long playlistId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        
        if (playlistOpt.isEmpty()) {
            throw new RuntimeException("Playlist not found with ID: " + playlistId);
        }
        
        Playlist playlist = playlistOpt.get();
        
        if (!playlist.isPublic()) {
            throw new RuntimeException("Playlist is private");
        }
        
        if (playlist.getSongs() == null) {
            return new ArrayList<>();
        }
        
        return playlist.getSongs().stream()
            .map(songService::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Helper method to convert Playlist to PlaylistDTO
    private PlaylistDTO convertToDTO(Playlist playlist) {
        // Get playlist owner username
        String ownerUsername = playlist.getUser() != null ? 
                              playlist.getUser().getUsername() : "Unknown";
        
        // Get song count
        int songCount = playlist.getSongs() != null ? playlist.getSongs().size() : 0;
        
        // Get follower count
        int followerCount = playlist.getFollowers() != null ? playlist.getFollowers().size() : 0;
        
        // Convert songs to DTOs
        List<SongDTO> songDTOs = new ArrayList<>();
        if (playlist.getSongs() != null) {
            songDTOs = playlist.getSongs().stream()
                .map(songService::convertToDTO)
                .collect(Collectors.toList());
        }
        
        return new PlaylistDTO(
            playlist.getId(),
            playlist.getName(),
            playlist.getDescription(),
            ownerUsername,
            playlist.getUser() != null ? playlist.getUser().getId() : null,
            playlist.isPublic(),
            songDTOs,
            playlist.getCreatedAt() != null ? playlist.getCreatedAt() : LocalDateTime.now(),
            playlist.getUpdatedAt() != null ? playlist.getUpdatedAt() : LocalDateTime.now(),
            songCount,
            followerCount
        );
    }
    
    // NEW: Get playlists by user
    public List<PlaylistDTO> getPlaylistsByUser(Long userId) {
        return playlistRepository.findByUserId(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}