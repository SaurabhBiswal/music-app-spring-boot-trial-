package com.music.musicapp.controller;

import com.music.musicapp.dto.ApiResponse;
import com.music.musicapp.dto.LoginRequest;
import com.music.musicapp.model.User;
import com.music.musicapp.model.PasswordResetToken;
import com.music.musicapp.repository.UserRepository;
import com.music.musicapp.repository.PasswordResetRepository;
import com.music.musicapp.repository.SongRepository;
import com.music.musicapp.repository.PlaylistRepository;
import com.music.musicapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    public static class ApiResponse {
        private String status;
        private String message;
        private Object data;
        
        public ApiResponse() {}
        
        public ApiResponse(String status, String message, Object data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }
        
        public static ApiResponse success(String message) {
            return new ApiResponse("success", message, null);
        }
        
        public static ApiResponse success(String message, Object data) {
            return new ApiResponse("success", message, data);
        }
        
        public static ApiResponse error(String message) {
            return new ApiResponse("error", message, null);
        }
        
        public static ApiResponse error(String message, Object data) {
            return new ApiResponse("error", message, data);
        }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SongRepository songRepository;
    
    @Autowired
    private PlaylistRepository playlistRepository;
    
    @Autowired
    private PasswordResetRepository passwordResetRepository;

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest) {
        String identifier = loginRequest.getUsernameOrEmail();
        String password = loginRequest.getPassword();

        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier, identifier);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                // ✅ Generate JWT token
                String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                
                // ✅ Create response with token
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("token", token);
                responseData.put("user", new HashMap<String, Object>() {{
                    put("id", user.getId());
                    put("username", user.getUsername());
                    put("email", user.getEmail());
                    put("role", user.getRole()); // ADMIN or USER
                }});
                
                return ResponseEntity.ok(ApiResponse.success("Login Successful", responseData));
            } else {
                return ResponseEntity.status(401).body(ApiResponse.error("Incorrect Password!"));
            }
        }
        return ResponseEntity.status(404).body(ApiResponse.error("User not found!"));
    }

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username already taken!"));
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email already registered!"));
        }
        
        // ✅ Set default role
        user.setRole("USER");
        
        User savedUser = userRepository.save(user);
        
        // ✅ Generate token for immediate login
        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole());
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("user", new HashMap<String, Object>() {{
            put("id", savedUser.getId());
            put("username", savedUser.getUsername());
            put("email", savedUser.getEmail());
            put("role", savedUser.getRole());
        }});
        
        return ResponseEntity.ok(ApiResponse.success("Account created successfully!", responseData));
    }
    
    // Statistics endpoint
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalSongs", songRepository.count());
        stats.put("totalPlaylists", playlistRepository.count());
        stats.put("totalUsers", userRepository.count());
        return ResponseEntity.ok(stats);
    }
    
    // ==================== FORGOT PASSWORD ENDPOINTS ====================
    
    // Step 1: Request password reset
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is required"));
            }
            
            Optional<User> userOpt = userRepository.findByEmail(email.trim());
            
            // Security: Always return success message even if user doesn't exist
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(
                    "If your email is registered, you will receive a password reset link"
                ));
            }
            
            User user = userOpt.get();
            
            // Delete any existing tokens for this user
            Optional<PasswordResetToken> existingToken = passwordResetRepository.findByUser(user);
            if (existingToken.isPresent()) {
                passwordResetRepository.delete(existingToken.get());
            }
            
            // Create new reset token
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUser(user);
            resetToken.setExpiryDate(new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000))); // 24 hours
            
            passwordResetRepository.save(resetToken);
            
            // Clean up expired tokens
            passwordResetRepository.deleteExpiredTokens(new Date());
            
            // In production: Send email with reset link
            // For demo: Return token in response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "Password reset link has been sent to your email");
            responseData.put("token", resetToken.getToken()); // Demo only - remove in production
            responseData.put("expiresIn", "24 hours");
            responseData.put("email", user.getEmail());
            
            System.out.println("🔐 FORGOT PASSWORD TOKEN GENERATED:");
            System.out.println("📧 Email: " + user.getEmail());
            System.out.println("🔑 Token: " + resetToken.getToken());
            System.out.println("⏰ Expires: " + resetToken.getExpiryDate());
            System.out.println("---------------------");
            
            return ResponseEntity.ok(ApiResponse.success(
                "Password reset instructions sent to email", 
                responseData
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error processing forgot password request: " + e.getMessage()));
        }
    }
    
    // Step 2: Verify reset token
    @GetMapping("/verify-reset-token/{token}")
    public ResponseEntity<ApiResponse> verifyResetToken(@PathVariable String token) {
        try {
            Optional<PasswordResetToken> tokenOpt = passwordResetRepository.findByToken(token);
            
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid reset token"));
            }
            
            PasswordResetToken resetToken = tokenOpt.get();
            
            // Check if token expired
            if (resetToken.getExpiryDate().before(new Date())) {
                passwordResetRepository.delete(resetToken);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Reset token has expired"));
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("valid", true);
            responseData.put("email", resetToken.getUser().getEmail());
            responseData.put("expiresAt", resetToken.getExpiryDate());
            
            return ResponseEntity.ok(ApiResponse.success("Token is valid", responseData));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error verifying token: " + e.getMessage()));
        }
    }
    
    // Step 3: Reset password with token
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam(required = false) String confirmPassword) {
        
        try {
            // Validation
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Reset token is required"));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("New password is required"));
            }
            
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password must be at least 6 characters"));
            }
            
            if (confirmPassword != null && !newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Passwords do not match"));
            }
            
            // Find token
            Optional<PasswordResetToken> tokenOpt = passwordResetRepository.findByToken(token.trim());
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired reset token"));
            }
            
            PasswordResetToken resetToken = tokenOpt.get();
            
            // Check if token expired
            if (resetToken.getExpiryDate().before(new Date())) {
                passwordResetRepository.delete(resetToken);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Reset token has expired. Please request a new one."));
            }
            
            // Update user password
            User user = resetToken.getUser();
            user.setPassword(newPassword.trim()); // Note: In production, use BCrypt encoding
            userRepository.save(user);
            
            // Delete used token
            passwordResetRepository.delete(resetToken);
            
            System.out.println("✅ PASSWORD RESET SUCCESSFUL:");
            System.out.println("📧 User: " + user.getEmail());
            System.out.println("🆔 User ID: " + user.getId());
            System.out.println("---------------------");
            
            return ResponseEntity.ok(ApiResponse.success(
                "Password has been reset successfully. You can now login with your new password."
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error resetting password: " + e.getMessage()));
        }
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Auth Service is running", null));
    }
}
