package com.music.musicapp.service;

import com.music.musicapp.model.PasswordResetToken;
import com.music.musicapp.model.User;
import com.music.musicapp.repository.PasswordResetRepository;
import com.music.musicapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordResetRepository tokenRepository;
    
    // Generate token and send email
    @Transactional
    public String createPasswordResetTokenForUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        // For security reasons, always return success message even if email doesn't exist
        if (userOptional.isEmpty()) {
            return "If an account exists with this email, a password reset link has been sent.";
        }
        
        User user = userOptional.get();
        
        // Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);
        
        // Create new reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        
        // Save token
        tokenRepository.save(resetToken);
        
        // In production, send email here
        // For now, print to console
        System.out.println("\n🔐 ========== PASSWORD RESET TOKEN ==========");
        System.out.println("📧 Email: " + email);
        System.out.println("👤 User: " + user.getUsername());
        System.out.println("🔑 Token: " + token);
        System.out.println("⏰ Expires: " + resetToken.getExpiryDate());
        System.out.println("🔗 Demo URL: http://localhost:3000/?token=" + token);
        System.out.println("==========================================\n");
        
        return "Password reset link has been sent to your email. Check console for demo token.";
    }
    
    // Validate token
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        
        if (tokenOptional.isEmpty()) {
            System.out.println("❌ Token not found: " + token);
            return false;
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        // Check if token is expired
        if (resetToken.isExpired()) {
            System.out.println("❌ Token expired: " + token);
            tokenRepository.delete(resetToken); // Clean up expired token
            return false;
        }
        
        System.out.println("✅ Token valid for user: " + resetToken.getUser().getEmail());
        return true;
    }
    
    // Reset password
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        
        if (tokenOptional.isEmpty()) {
            System.out.println("❌ Reset failed: Token not found");
            return false;
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        // Check if token is expired
        if (resetToken.isExpired()) {
            System.out.println("❌ Reset failed: Token expired");
            tokenRepository.delete(resetToken);
            return false;
        }
        
        User user = resetToken.getUser();
        
        System.out.println("\n✅ ========== PASSWORD RESET ==========");
        System.out.println("👤 User: " + user.getUsername() + " (" + user.getEmail() + ")");
        System.out.println("🔑 Old password: " + user.getPassword());
        System.out.println("🔐 New password: " + newPassword);
        
        // Update password (simple storage - no encoding)
        user.setPassword(newPassword);
        userRepository.save(user);
        
        System.out.println("✅ Password updated in database");
        
        // Delete the used token
        tokenRepository.delete(resetToken);
        
        // Clean up any expired tokens
        tokenRepository.deleteExpiredTokens(new Date());
        
        System.out.println("✅ Token deleted after use");
        System.out.println("===================================\n");
        
        return true;
    }
    
    // Get user email from token (for frontend to display)
    public Optional<String> getUserEmailFromToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        
        if (tokenOptional.isEmpty()) {
            return Optional.empty();
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            return Optional.empty();
        }
        
        return Optional.of(resetToken.getUser().getEmail());
    }
}