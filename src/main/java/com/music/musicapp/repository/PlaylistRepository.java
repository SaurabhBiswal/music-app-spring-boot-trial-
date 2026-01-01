package com.music.musicapp.repository;

import com.music.musicapp.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    
    List<Playlist> findByUserId(Long userId);
    List<Playlist> findByNameContainingIgnoreCase(String name);
    List<Playlist> findByIsPublicTrue();
    
    // ADDED for PlaylistService
    List<Playlist> findByIsPublicTrueAndNameContainingIgnoreCase(String name);
    
    // Custom queries
    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true ORDER BY SIZE(p.followers) DESC")
    List<Playlist> findPopularPlaylists();
    
    @Query("SELECT p FROM Playlist p WHERE p.user.id = :userId AND p.isPublic = true")
    List<Playlist> findPublicPlaylistsByUser(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Playlist p JOIN p.songs s WHERE s.id = :songId")
    List<Playlist> findPlaylistsContainingSong(@Param("songId") Long songId);
}