package com.music.musicapp.service;

import com.music.musicapp.dto.SongDTO;
import com.music.musicapp.model.Song;
import com.music.musicapp.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SongService {
    
    @Autowired
    private SongRepository songRepository;
    
    // Get all songs
    public List<SongDTO> getAllSongs() {
        return songRepository.findAll().stream()
            .map(this::convertToDTO)
            .toList();
    }
    
    // Get song by ID
    public SongDTO getSongById(Long id) {
        Optional<Song> songOpt = songRepository.findById(id);
        
        if (songOpt.isEmpty()) {
            throw new RuntimeException("Song not found with ID: " + id);
        }
        
        return convertToDTO(songOpt.get());
    }
    
    // Search songs by title
    public List<SongDTO> searchByTitle(String title) {
        return songRepository.findByTitleContainingIgnoreCase(title).stream()
            .map(this::convertToDTO)
            .toList();
    }
    
    // Search songs by artist
    public List<SongDTO> searchByArtist(String artist) {
        return songRepository.findByArtistContainingIgnoreCase(artist).stream()
            .map(this::convertToDTO)
            .toList();
    }
    
    // Get songs by genre
    public List<SongDTO> getSongsByGenre(String genre) {
        return songRepository.findByGenre(genre).stream()
            .map(this::convertToDTO)
            .toList();
    }
    
    // Get songs by album
    public List<SongDTO> getSongsByAlbum(String album) {
        return songRepository.findByAlbumContainingIgnoreCase(album).stream()
            .map(this::convertToDTO)
            .toList();
    }
    
    // Helper method to convert Song entity to SongDTO
    private SongDTO convertToDTO(Song song) {
        return new SongDTO(
            song.getId(),
            song.getTitle(),
            song.getArtist(),
            song.getAlbum(),
            song.getReleaseYear(),
            song.getDurationSeconds(),
            song.getFilePath(),
            song.getGenre(),
            song.getUploadedAt()
        );
    }
    
    // Get recently added songs (for homepage)
    public List<SongDTO> getRecentSongs(int limit) {
        // Simple implementation - get all and limit
        // In production, you'd add createdAt timestamp and order by it
        return songRepository.findAll().stream()
            .limit(limit)
            .map(this::convertToDTO)
            .toList();
    }
}