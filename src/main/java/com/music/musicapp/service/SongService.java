package com.music.musicapp.service;

import com.music.musicapp.dto.SongDTO;
import com.music.musicapp.model.Song;
import com.music.musicapp.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SongService {
    
    @Autowired
    private SongRepository songRepository;
    
    public List<SongDTO> getAllSongs() {
        return songRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public SongDTO getSongById(Long id) {
        Optional<Song> songOpt = songRepository.findById(id);
        if (songOpt.isEmpty()) {
            throw new RuntimeException("Song not found with ID: " + id);
        }
        return convertToDTO(songOpt.get());
    }
    
    public List<SongDTO> searchByTitle(String title) {
        return songRepository.findByTitleContainingIgnoreCase(title).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<SongDTO> searchByArtist(String artist) {
        return songRepository.findByArtistContainingIgnoreCase(artist).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<SongDTO> getSongsByGenre(String genre) {
        return songRepository.findByGenreContainingIgnoreCase(genre).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<SongDTO> getSongsByAlbum(String album) {
        return songRepository.findByAlbumContainingIgnoreCase(album).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<SongDTO> getRecentSongs(int limit) {
        return songRepository.findByOrderByUploadedAtDesc().stream()
            .limit(limit)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public SongDTO updateSongFilePath(Long songId, String audioFilePath) {
        Optional<Song> songOpt = songRepository.findById(songId);
        if (songOpt.isEmpty()) {
            throw new RuntimeException("Song not found with ID: " + songId);
        }
        Song song = songOpt.get();
        song.setAudioUrl(audioFilePath); // Fixed: Use audioUrl instead of audioFilePath
        Song updatedSong = songRepository.save(song);
        return convertToDTO(updatedSong);
    }
    
    public List<SongDTO> getSongsWithAudio() {
        return songRepository.findAll().stream()
            .filter(song -> song.getAudioUrl() != null && !song.getAudioUrl().isEmpty())
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public SongDTO convertToDTO(Song song) {
        return new SongDTO(
            song.getId(),
            song.getTitle(),
            song.getArtist(),
            song.getAlbum(),
            song.getGenre(),
            song.getDuration(),
            song.getAudioUrl(),
            song.getAlbumArtUrl(),
            song.getReleaseYear(),
            song.getUploadedAt(),
            song.getUploaderId(),
            song.getAverageRating(),
            song.getRatingCount(),
            song.getPlayCount(),
            song.getIsOfflineAvailable()
        );
    }
    
    // NEW: Search songs by query (title, artist, or album)
    public List<SongDTO> searchSongs(String query) {
        return songRepository.searchSongs(query).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // NEW: Get trending songs
    public List<SongDTO> getTrendingSongs(int limit) {
        return songRepository.findByOrderByPlayCountDesc().stream()
            .limit(limit)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}