package com.music.musicapp.service;

import com.music.musicapp.dto.RegisterRequest;
import com.music.musicapp.dto.UserDTO;
import com.music.musicapp.model.User;
import com.music.musicapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Register a new user
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
        User user = new User(
            request.getUsername(),
            request.getEmail(),
            request.getPassword(), // In real app, encrypt this!
            request.getFullName()
        );
        
        // Save to database
        User savedUser = userRepository.save(user);
        
        // Convert to DTO and return
        return new UserDTO(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFullName(),
            savedUser.getCreatedAt()
        );
    }
    
    // Login user (simple version - Day 6 will add JWT)
    public UserDTO loginUser(String usernameOrEmail, String password) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found: " + usernameOrEmail);
        }
        
        User user = userOpt.get();
        
        // Check password (in real app, use password encoder)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        
        // Convert to DTO and return
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getCreatedAt()
        );
    }
    
    // Get user by ID
    public UserDTO getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + id);
        }
        
        User user = userOpt.get();
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getCreatedAt()
        );
    }
    
    // Get all users (for testing)
    public java.util.List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(user -> new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getCreatedAt()
            ))
            .toList();
    }
}