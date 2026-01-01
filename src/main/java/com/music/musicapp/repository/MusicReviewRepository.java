package com.music.musicapp.repository;

import com.music.musicapp.model.MusicReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MusicReviewRepository extends JpaRepository<MusicReview, Long> {
    
    List<MusicReview> findByTrackIdOrderByCreatedAtDesc(String trackId);
    
    List<MusicReview> findBySongIdOrderByCreatedAtDesc(Long songId);
    
    List<MusicReview> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Optional<MusicReview> findByUserIdAndTrackId(Long userId, String trackId);
    
    Optional<MusicReview> findByUserIdAndSongId(Long userId, Long songId);
    
    @Query("SELECT AVG(r.rating) FROM MusicReview r WHERE r.trackId = :trackId")
    Double findAverageRatingByTrackId(@Param("trackId") String trackId);
    
    @Query("SELECT AVG(r.rating) FROM MusicReview r WHERE r.songId = :songId")
    Double findAverageRatingBySongId(@Param("songId") Long songId);
    
    Long countByTrackId(String trackId);
    
    Long countBySongId(Long songId);
    
    Long countByUserId(Long userId);
}