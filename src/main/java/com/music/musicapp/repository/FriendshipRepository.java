package com.music.musicapp.repository;

import com.music.musicapp.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
    
    List<Friendship> findByUserIdAndStatus(Long userId, String status);
    
    List<Friendship> findByFriendIdAndStatus(Long friendId, String status);
    
    @Query("SELECT f.friend.id FROM Friendship f WHERE f.user.id = :userId AND f.status = 'ACCEPTED'")
    List<Long> findFriendIdsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.user.id = :userId AND f.status = 'ACCEPTED'")
    Long countFriendsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.friend.id = :userId AND f.status = 'ACCEPTED'")
    Long countFollowersByUserId(@Param("userId") Long userId);
}