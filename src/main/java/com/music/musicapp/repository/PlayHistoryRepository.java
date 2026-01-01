package com.music.musicapp.repository;

import com.music.musicapp.model.PlayHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {
    
    @Query("SELECT ph FROM PlayHistory ph WHERE ph.user.id = :userId")
    List<PlayHistory> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ph FROM PlayHistory ph WHERE ph.user.id = :userId ORDER BY ph.playedAt DESC")
    List<PlayHistory> findByUserIdOrderByPlayedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ph) FROM PlayHistory ph WHERE ph.songId = :songId")
    Long countBySongId(@Param("songId") Long songId);
    
    @Query("SELECT CASE WHEN COUNT(ph) > 0 THEN true ELSE false END FROM PlayHistory ph WHERE ph.user.id = :userId AND ph.songId = :songId")
    boolean existsByUserIdAndSongId(@Param("userId") Long userId, @Param("songId") Long songId);
    
    List<PlayHistory> findBySongId(Long songId);
    
    @Query("SELECT ph.songId, COUNT(ph) as playCount FROM PlayHistory ph WHERE ph.playedAt >= :since GROUP BY ph.songId ORDER BY playCount DESC")
    List<Object[]> findTrendingSince(@Param("since") LocalDateTime since);
}