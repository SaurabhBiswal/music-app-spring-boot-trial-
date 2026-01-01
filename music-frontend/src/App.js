import React, { useState, useEffect } from 'react';
import { searchSongs, getRecentSongs } from './api';
import { Play, Pause, Search, Music, Disc, Plus } from 'lucide-react';

function App() {
  const [query, setQuery] = useState('');
  const [songs, setSongs] = useState([]);
  const [currentSong, setCurrentSong] = useState(null);
  const [isPlaying, setIsPlaying] = useState(false);

  useEffect(() => {
    loadFeatured();
  }, []);

  const loadFeatured = async () => {
    try {
      const res = await getRecentSongs();
      setSongs(res.data.data);
    } catch (e) { console.log("Recent songs load nahi huye"); }
  };

  const handleSearch = async () => {
    if(!query) return;
    const res = await searchSongs(query);
    setSongs(res.data.data);
  };

  const playSong = (song) => {
    if (!song.audioUrl) {
        alert("Bhai, is gaane ka audio link database mein nahi hai!");
        return;
    }
    setCurrentSong(song);
    setIsPlaying(true);
    const audio = document.querySelector('audio');
    if (audio) {
        audio.src = song.audioUrl;
        audio.load();
        audio.play().catch(e => console.error("Playback failed:", e));
    }
  };

  // --- DAY 12: Playlist Logic ---
const addToPlaylist = async (song) => {
    try {
        console.log("Adding song:", song.title); // Console mein check karne ke liye
        
        const response = await fetch(`http://localhost:8080/api/playlists/1/songs`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                title: song.title,
                artist: song.artist,
                album: song.album,
                audioUrl: song.audioUrl,
                albumArtUrl: song.albumArtUrl
            }) 
        });

        if (response.ok) {
            alert(`✅ ${song.title} playlist mein add ho gaya!`);
        } else {
            const errorData = await response.json();
            alert(`❌ Error: ${errorData.message || 'Nahi ho paya'}`);
        }
    } catch (error) {
        console.error("Playlist API Error:", error);
        alert("❌ Backend band hai ya network error hai!");
    }
};

  return (
    <div style={styles.container}>
      {/* Sidebar */}
      <div style={styles.sidebar}>
        <h2 style={{color: '#1DB954'}}><Music /> MusicApp</h2>
        <nav style={styles.nav}>
          <div style={styles.navItem}><Disc size={20}/> Home</div>
          <div style={styles.navItem}><Search size={20}/> Search</div>
        </nav>
      </div>

      {/* Main Area */}
      <div style={styles.main}>
        <div style={styles.searchBar}>
          <input 
            type="text" 
            placeholder="Shakira, Arijit Singh..." 
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            style={styles.input}
          />
          <button onClick={handleSearch} style={styles.searchBtn}><Search size={20}/></button>
        </div>

        <h3>Recommended for you</h3>
        <div style={styles.songGrid}>
          {songs.map((song, index) => (
            <div key={index} style={styles.card} onClick={() => playSong(song)}>
              <img src={song.albumArtUrl || 'https://via.placeholder.com/150'} style={styles.albumArt} alt="art" />
              
              {/* + Button for Playlist */}
              <button 
    onClick={(e) => { 
        e.stopPropagation(); // Isse gaana play nahi hoga
        console.log("Button clicked!"); // Test ke liye
        addToPlaylist(song); 
    }} 
    style={styles.addBtn}
>
    <Plus size={18} color="black" />
</button>

              <div style={styles.songTitle}>{song.title}</div>
              <div style={styles.songArtist}>{song.artist}</div>
              <div style={styles.playIcon}><Play fill="black" size={20} /></div>
            </div>
          ))}
        </div>
      </div>

      {/* Player Bar */}
      {currentSong && (
        <div style={styles.playerBar}>
          <div style={{display:'flex', alignItems:'center', gap:'15px', width: '30%'}}>
             <img src={currentSong.albumArtUrl} style={{width:'50px', borderRadius:'4px'}} alt="thumb" />
             <div style={{overflow:'hidden'}}>
                <div style={{fontSize:'14px', fontWeight:'bold', whiteSpace:'nowrap'}}>{currentSong.title}</div>
                <div style={{fontSize:'12px', color:'#b3b3b3'}}>{currentSong.artist}</div>
             </div>
          </div>
          <div style={{width: '40%', textAlign: 'center'}}>
            <audio controls autoPlay src={currentSong.audioUrl} style={{width: '100%', height: '35px'}}></audio>
          </div>
          <div style={{width: '30%'}}></div>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: { backgroundColor: '#000', color: 'white', minHeight: '100vh', display: 'flex' },
  sidebar: { width: '240px', backgroundColor: '#000', padding: '24px', borderRight: '1px solid #282828' },
  nav: { marginTop: '30px' },
  navItem: { display: 'flex', gap: '15px', marginBottom: '20px', cursor: 'pointer', fontWeight: 'bold' },
  main: { flex: 1, backgroundColor: '#121212', padding: '30px', overflowY: 'auto', paddingBottom: '100px' },
  searchBar: { display: 'flex', gap: '10px', marginBottom: '40px' },
  input: { padding: '12px 20px', borderRadius: '25px', width: '400px', border: 'none', backgroundColor: '#242424', color: 'white' },
  searchBtn: { backgroundColor: '#1DB954', border: 'none', borderRadius: '50%', padding: '10px', cursor: 'pointer' },
  songGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '24px' },
  card: { backgroundColor: '#181818', padding: '16px', borderRadius: '8px', cursor: 'pointer', position: 'relative', transition: '0.3s' },
  albumArt: { width: '100%', borderRadius: '4px', marginBottom: '12px' },
  songTitle: { fontSize: '16px', fontWeight: 'bold', marginBottom: '8px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' },
  songArtist: { fontSize: '14px', color: '#b3b3b3' },
  addBtn: {
    position: 'absolute', top: '20px', right: '20px', backgroundColor: '#1DB954', border: 'none', 
    borderRadius: '50%', width: '32px', height: '32px', cursor: 'pointer', display: 'flex', 
    alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 8px rgba(0,0,0,0.3)'
  },
  playerBar: { position: 'fixed', bottom: 0, left: 0, width: '100%', background: '#181818', padding: '15px 30px', borderTop: '1px solid #282828', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }
};

export default App;