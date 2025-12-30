package com.music.musicapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(name = "created_at")
    private String createdAt;
    
    // Many playlists belong to one user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Many-to-many relationship with songs
    @ManyToMany
    @JoinTable(
        name = "playlist_songs",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> songs = new ArrayList<>();
    
    // Constructor without id and songs list
    public Playlist(String name, String description, User user) {
        this.name = name;
        this.description = description;
        this.user = user;
        this.createdAt = java.time.LocalDateTime.now().toString();
        this.songs = new ArrayList<>();
    }
    
    // Helper method to add a song to playlist
    public void addSong(Song song) {
        this.songs.add(song);
    }
    
    // Helper method to remove a song from playlist
    public void removeSong(Song song) {
        this.songs.remove(song);
    }
    
    // Helper method to get song count
    public int getSongCount() {
        return this.songs.size();
    }
}