package com.music.musicapp.service;

import com.music.musicapp.dto.RegisterRequest;
import com.music.musicapp.dto.UserDTO;
import com.music.musicapp.model.User;
import com.music.musicapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Register a new user
    @Transactional
    public UserDTO registerUser(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // In real app, encrypt this!
        user.setFullName(request.getFullName());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        
        // Save to database
        User savedUser = userRepository.save(user);
        
        // Convert to DTO and return
        return convertToDTO(savedUser);
    }
    
    // Login user (simple version - Day 6 will add JWT)
    public UserDTO loginUser(String usernameOrEmail, String password) {
        Optional<User> userOpt;
        
        // Check if input is email or username
        if (usernameOrEmail.contains("@")) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        } else {
            userOpt = userRepository.findByUsername(usernameOrEmail);
        }
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found: " + usernameOrEmail);
        }
        
        User user = userOpt.get();
        
        // Check password (in real app, use password encoder)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Convert to DTO and return
        return convertToDTO(user);
    }
    
    // Get user by ID
    public UserDTO getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + id);
        }
        
        User user = userOpt.get();
        return convertToDTO(user);
    }
    
    // Get all users (for testing)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Helper method to convert User to UserDTO
    private UserDTO convertToDTO(User user) {
        // Calculate follower/following counts (you need to implement these)
        int followerCount = user.getFollowers() != null ? user.getFollowers().size() : 0;
        int followingCount = user.getFollowing() != null ? user.getFollowing().size() : 0;
        int playlistCount = user.getOwnedPlaylists() != null ? user.getOwnedPlaylists().size() : 0;
        int reviewCount = user.getRatings() != null ? user.getRatings().size() : 0;
        
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getProfilePicture(),
            user.getBio(),
            user.getCreatedAt(),
            user.getLastLogin(),
            followerCount,
            followingCount,
            playlistCount,
            reviewCount
        );
    }
    
    // NEW: Update user profile
    @Transactional
    public UserDTO updateUserProfile(Long userId, String bio, String profilePicture) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        
        User user = userOpt.get();
        
        if (bio != null) {
            user.setBio(bio);
        }
        
        if (profilePicture != null) {
            user.setProfilePicture(profilePicture);
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        
        return convertToDTO(updatedUser);
    }
}