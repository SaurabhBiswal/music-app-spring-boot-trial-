package com.music.musicapp.repository;

import com.music.musicapp.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    @Query("SELECT r FROM Rating r WHERE r.user.id = :userId AND r.trackId = :trackId")
    Optional<Rating> findByUserIdAndTrackId(@Param("userId") Long userId, @Param("trackId") String trackId);
    
    @Query("SELECT r FROM Rating r WHERE r.user.id = :userId")
    List<Rating> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    List<Rating> findByTrackIdOrderByRatedAtDesc(String trackId);
    
    Long countByTrackId(String trackId);
    
    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.trackId = :trackId")
    Double findAverageRatingByTrackId(@Param("trackId") String trackId);
    
    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.songId = :songId")
    Double findAverageRatingBySongId(@Param("songId") Long songId);
    
    @Query("SELECT r FROM Rating r WHERE r.user.id = :userId AND r.ratingValue >= :minRating")
    List<Rating> findByUserIdAndRatingValueGreaterThanEqual(@Param("userId") Long userId, @Param("minRating") Double minRating);
    
    // Return empty list to avoid errors
    default List<Object[]> findSimilarUsers(Long userId) {
        return List.of();
    }
    
    // Return empty list to avoid errors
    default List<Object[]> findUsersWhoLikeGenre(String genre, Long userId) {
        return List.of();
    }
}