-- Insert sample users
INSERT INTO users (username, email, password, full_name, created_at) VALUES
('john_doe', 'john@example.com', 'password123', 'John Doe', '2024-01-15T10:30:00'),
('jane_smith', 'jane@example.com', 'password456', 'Jane Smith', '2024-01-16T11:45:00'),
('music_lover', 'music@example.com', 'music123', 'Music Lover', '2024-01-17T14:20:00');

-- Insert sample songs
INSERT INTO songs (title, artist, album, release_year, duration_seconds, file_path, genre, uploaded_at) VALUES
('Blinding Lights', 'The Weeknd', 'After Hours', 2020, 200, '/audio/weeknd_blinding.mp3', 'Pop', '2024-01-10T09:15:00'),
('Shape of You', 'Ed Sheeran', '÷ (Divide)', 2017, 234, '/audio/ed_shape.mp3', 'Pop', '2024-01-11T10:30:00'),
('Bohemian Rhapsody', 'Queen', 'A Night at the Opera', 1975, 354, '/audio/queen_bohemian.mp3', 'Rock', '2024-01-12T11:45:00'),
('Billie Jean', 'Michael Jackson', 'Thriller', 1982, 294, '/audio/mj_billie.mp3', 'Pop', '2024-01-13T12:00:00'),
('Smells Like Teen Spirit', 'Nirvana', 'Nevermind', 1991, 301, '/audio/nirvana_smells.mp3', 'Rock', '2024-01-14T13:15:00');

-- Insert sample playlists
INSERT INTO playlists (name, description, user_id, created_at) VALUES
('My Favorites', 'All my favorite songs', 1, '2024-01-18T15:30:00'),
('Workout Mix', 'High energy songs for workout', 2, '2024-01-19T16:45:00'),
('Chill Vibes', 'Relaxing music', 1, '2024-01-20T17:00:00');

-- Add songs to playlists (playlist_songs junction table)
INSERT INTO playlist_songs (playlist_id, song_id) VALUES
(1, 1), -- Blinding Lights to My Favorites
(1, 2), -- Shape of You to My Favorites
(1, 3), -- Bohemian Rhapsody to My Favorites
(2, 4), -- Billie Jean to Workout Mix
(2, 5), -- Smells Like Teen Spirit to Workout Mix
(3, 1), -- Blinding Lights to Chill Vibes
(3, 2); -- Shape of You to Chill Vibes