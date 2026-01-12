package com.music.musicapp.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 255)
    private String token;
    
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;
    
    // Default constructor
    public PasswordResetToken() {
    }
    
    // Constructor with parameters
    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        // Set expiry to 24 hours from now
        this.expiryDate = new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
    }
    
    // Check if token is expired
    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", expiryDate=" + expiryDate +
                '}';
    }
}