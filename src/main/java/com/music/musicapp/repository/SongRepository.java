package com.music.musicapp.repository;

import com.music.musicapp.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    
    // Find songs by title (case-insensitive search)
    List<Song> findByTitleContainingIgnoreCase(String title);
    
    // Find songs by artist (case-insensitive search)
    List<Song> findByArtistContainingIgnoreCase(String artist);
    
    // Find songs by genre
    List<Song> findByGenre(String genre);
    
    // Find songs by album
    List<Song> findByAlbumContainingIgnoreCase(String album);
    
    // Find songs by release year
    List<Song> findByReleaseYear(Integer releaseYear);
    
    // Find songs with duration less than specified seconds
    List<Song> findByDurationSecondsLessThanEqual(Integer duration);
}