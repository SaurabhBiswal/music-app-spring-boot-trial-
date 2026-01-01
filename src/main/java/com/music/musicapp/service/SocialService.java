package com.music.musicapp.service;

import com.music.musicapp.model.*;
import com.music.musicapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SocialService {
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MusicReviewRepository musicReviewRepository;
    
    @Autowired
    private UserActivityRepository userActivityRepository;
    
    @Autowired
    private FriendshipRepository friendshipRepository;
    
    @Autowired
    private SongRepository songRepository;
    
    // Add rating and review
    public Map<String, Object> addRatingAndReview(Long userId, String trackId, 
                                                  Double rating, String review, 
                                                  String reviewTitle) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                result.put("success", false);
                result.put("message", "User not found");
                return result;
            }
            
            User user = userOpt.get();
            
            // Check if user already reviewed this track
            Optional<MusicReview> existingReview = musicReviewRepository.findByUserIdAndTrackId(userId, trackId);
            
            MusicReview reviewEntity;
            if (existingReview.isPresent()) {
                // Update existing review
                reviewEntity = existingReview.get();
                reviewEntity.setRating(rating.intValue());
                reviewEntity.setReviewContent(review);
                reviewEntity.setReviewTitle(reviewTitle);
                reviewEntity.setUpdatedAt(LocalDateTime.now());
            } else {
                // Create new review
                reviewEntity = new MusicReview();
                reviewEntity.setUser(user);
                reviewEntity.setTrackId(trackId);
                reviewEntity.setTrackTitle("Track"); // This should come from track info
                reviewEntity.setArtistName("Artist"); // This should come from track info
                reviewEntity.setRating(rating.intValue());
                reviewEntity.setReviewContent(review);
                reviewEntity.setReviewTitle(reviewTitle);
                reviewEntity.setHelpfulCount(0);
                reviewEntity.setCommentCount(0);
            }
            
            musicReviewRepository.save(reviewEntity);
            
            // Create activity
            UserActivity activity = new UserActivity();
            activity.setUser(user);
            activity.setActivityType("RATED");
            activity.setTargetType("TRACK");
            activity.setTargetId(trackId);
            activity.setTargetName("Track");
            activity.setDetails("Rated " + rating + " stars: " + reviewTitle);
            userActivityRepository.save(activity);
            
            result.put("success", true);
            result.put("reviewId", reviewEntity.getId());
            result.put("message", "Review submitted successfully");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error submitting review: " + e.getMessage());
        }
        
        return result;
    }
    
    // Get user activity feed
    public List<Map<String, Object>> getActivityFeed(Long userId, int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        try {
            // Get user's friends
            List<Long> friendIds = friendshipRepository.findFriendIdsByUserId(userId);
            friendIds.add(userId); // Include user's own activities
            
            // Get activities from user and friends
            List<UserActivity> userActivities = userActivityRepository.findByUserIdsOrderByCreatedAtDesc(friendIds);
            
            // Convert to response format
            for (UserActivity activity : userActivities.stream().limit(limit).collect(Collectors.toList())) {
                Map<String, Object> activityMap = new HashMap<>();
                activityMap.put("id", activity.getId());
                activityMap.put("userId", activity.getUser().getId());
                activityMap.put("username", activity.getUser().getUsername());
                activityMap.put("activityType", activity.getActivityType());
                activityMap.put("targetType", activity.getTargetType());
                activityMap.put("targetId", activity.getTargetId());
                activityMap.put("targetName", activity.getTargetName());
                activityMap.put("details", activity.getDetails());
                activityMap.put("createdAt", activity.getCreatedAt());
                
                // Calculate time ago
                activityMap.put("timeAgo", getTimeAgo(activity.getCreatedAt()));
                
                activities.add(activityMap);
            }
            
        } catch (Exception e) {
            System.err.println("Error getting activity feed: " + e.getMessage());
        }
        
        return activities;
    }
    
    // Get recent reviews
    public List<Map<String, Object>> getRecentReviews(int limit) {
        List<Map<String, Object>> reviews = new ArrayList<>();
        
        try {
            List<MusicReview> recentReviews = musicReviewRepository.findAll()
                .stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
            
            for (MusicReview review : recentReviews) {
                Map<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("id", review.getId());
                reviewMap.put("userId", review.getUser().getId());
                reviewMap.put("username", review.getUser().getUsername());
                reviewMap.put("trackId", review.getTrackId());
                reviewMap.put("trackTitle", review.getTrackTitle());
                reviewMap.put("artistName", review.getArtistName());
                reviewMap.put("rating", review.getRating());
                reviewMap.put("reviewTitle", review.getReviewTitle());
                reviewMap.put("reviewContent", review.getReviewContent());
                reviewMap.put("helpfulCount", review.getHelpfulCount());
                reviewMap.put("commentCount", review.getCommentCount());
                reviewMap.put("createdAt", review.getCreatedAt());
                reviewMap.put("timeAgo", getTimeAgo(review.getCreatedAt()));
                
                reviews.add(reviewMap);
            }
            
        } catch (Exception e) {
            System.err.println("Error getting recent reviews: " + e.getMessage());
        }
        
        return reviews;
    }
    
    // Get user's friends
    public List<Map<String, Object>> getUserFriends(Long userId) {
        List<Map<String, Object>> friends = new ArrayList<>();
        
        try {
            List<Friendship> friendships = friendshipRepository.findByUserIdAndStatus(userId, "ACCEPTED");
            
            for (Friendship friendship : friendships) {
                User friend = friendship.getFriend();
                Map<String, Object> friendMap = new HashMap<>();
                friendMap.put("id", friend.getId());
                friendMap.put("username", friend.getUsername());
                friendMap.put("avatarUrl", friend.getProfilePicture());
                friendMap.put("bio", friend.getBio());
                friendMap.put("friendSince", friendship.getCreatedAt());
                
                // Get friend's current activity (simplified)
                friendMap.put("status", "Listening to music");
                friendMap.put("lastSeen", getTimeAgo(friend.getLastLogin()));
                
                friends.add(friendMap);
            }
            
        } catch (Exception e) {
            System.err.println("Error getting user friends: " + e.getMessage());
        }
        
        return friends;
    }
    
    // Send friend request
    public Map<String, Object> sendFriendRequest(Long userId, Long friendId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<User> friendOpt = userRepository.findById(friendId);
            
            if (userOpt.isEmpty() || friendOpt.isEmpty()) {
                result.put("success", false);
                result.put("message", "User not found");
                return result;
            }
            
            // Check if friendship already exists
            Optional<Friendship> existingFriendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
            if (existingFriendship.isPresent()) {
                result.put("success", false);
                result.put("message", "Friend request already exists");
                return result;
            }
            
            // Create friendship
            Friendship friendship = new Friendship();
            friendship.setUser(userOpt.get());
            friendship.setFriend(friendOpt.get());
            friendship.setStatus("PENDING");
            
            friendshipRepository.save(friendship);
            
            // Create activity
            UserActivity activity = new UserActivity();
            activity.setUser(userOpt.get());
            activity.setActivityType("FRIEND_REQUEST");
            activity.setTargetType("USER");
            activity.setTargetId(friendId.toString());
            activity.setTargetName(friendOpt.get().getUsername());
            activity.setDetails("Sent friend request");
            userActivityRepository.save(activity);
            
            result.put("success", true);
            result.put("message", "Friend request sent");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error sending friend request: " + e.getMessage());
        }
        
        return result;
    }
    
    // Accept friend request
    public Map<String, Object> acceptFriendRequest(Long userId, Long friendId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Friendship> friendshipOpt = friendshipRepository.findByUserIdAndFriendId(friendId, userId);
            
            if (friendshipOpt.isEmpty()) {
                result.put("success", false);
                result.put("message", "Friend request not found");
                return result;
            }
            
            Friendship friendship = friendshipOpt.get();
            friendship.setStatus("ACCEPTED");
            friendship.setAcceptedAt(LocalDateTime.now());
            friendshipRepository.save(friendship);
            
            // Create activity
            User user = userRepository.findById(userId).orElseThrow();
            UserActivity activity = new UserActivity();
            activity.setUser(user);
            activity.setActivityType("FRIEND_ACCEPTED");
            activity.setTargetType("USER");
            activity.setTargetId(friendId.toString());
            activity.setTargetName(friendship.getUser().getUsername());
            activity.setDetails("Accepted friend request");
            userActivityRepository.save(activity);
            
            result.put("success", true);
            result.put("message", "Friend request accepted");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error accepting friend request: " + e.getMessage());
        }
        
        return result;
    }
    
    // Get user profile with stats
    public Map<String, Object> getUserProfile(Long userId) {
        Map<String, Object> profile = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                profile.put("id", user.getId());
                profile.put("username", user.getUsername());
                profile.put("email", user.getEmail());
                profile.put("avatarUrl", user.getProfilePicture());
                profile.put("bio", user.getBio());
                profile.put("joinDate", user.getCreatedAt());
                profile.put("lastLogin", user.getLastLogin());
                
                // Stats
                Long followerCount = friendshipRepository.countFollowersByUserId(userId);
                Long followingCount = friendshipRepository.countFriendsByUserId(userId);
                Long reviewCount = musicReviewRepository.countByUserId(userId);
                Long playlistCount = user.getOwnedPlaylists() != null ? (long) user.getOwnedPlaylists().size() : 0;
                
                profile.put("followerCount", followerCount);
                profile.put("followingCount", followingCount);
                profile.put("reviewCount", reviewCount);
                profile.put("playlistCount", playlistCount);
                
                // Recent activity
                List<Map<String, Object>> recentActivity = getActivityFeed(userId, 5);
                profile.put("recentActivity", recentActivity);
                
                profile.put("success", true);
            } else {
                profile.put("success", false);
                profile.put("message", "User not found");
            }
        } catch (Exception e) {
            profile.put("success", false);
            profile.put("message", "Error getting profile: " + e.getMessage());
        }
        
        return profile;
    }
    
    // Share track on social media
    public Map<String, Object> shareTrack(String trackId, String platform, 
                                         String message, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Generate share URLs
            Map<String, String> shareUrls = generateShareUrls(trackId, platform, message);
            
            // Record share activity
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                UserActivity activity = new UserActivity();
                activity.setUser(userOpt.get());
                activity.setActivityType("SHARED");
                activity.setTargetType("TRACK");
                activity.setTargetId(trackId);
                activity.setTargetName("Track");
                activity.setDetails("Shared on " + platform + ": " + message);
                userActivityRepository.save(activity);
            }
            
            result.put("success", true);
            result.put("shareUrls", shareUrls);
            result.put("message", "Shared successfully");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error sharing: " + e.getMessage());
        }
        
        return result;
    }
    
    // Helper method to get time ago
    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";
        
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(dateTime, now).getSeconds();
        
        if (seconds < 60) return "Just now";
        if (seconds < 3600) return (seconds / 60) + " minutes ago";
        if (seconds < 86400) return (seconds / 3600) + " hours ago";
        if (seconds < 604800) return (seconds / 86400) + " days ago";
        return (seconds / 604800) + " weeks ago";
    }
    
    private Map<String, String> generateShareUrls(String trackId, String platform, String message) {
        Map<String, String> shareUrls = new HashMap<>();
        String baseUrl = "https://musicapp.com/track/" + trackId;
        
        try {
            String encodedMessage = java.net.URLEncoder.encode(message, "UTF-8");
            String encodedUrl = java.net.URLEncoder.encode(baseUrl, "UTF-8");
            
            switch (platform.toLowerCase()) {
                case "twitter":
                    shareUrls.put("twitter", "https://twitter.com/intent/tweet?text=" + 
                        encodedMessage + "&url=" + encodedUrl);
                    break;
                case "facebook":
                    shareUrls.put("facebook", "https://www.facebook.com/sharer/sharer.php?u=" + 
                        encodedUrl + "&quote=" + encodedMessage);
                    break;
                case "whatsapp":
                    shareUrls.put("whatsapp", "https://api.whatsapp.com/send?text=" + 
                        encodedMessage + " " + encodedUrl);
                    break;
                default:
                    shareUrls.put("copy", baseUrl);
            }
        } catch (Exception e) {
            shareUrls.put("copy", baseUrl);
        }
        
        return shareUrls;
    }
}