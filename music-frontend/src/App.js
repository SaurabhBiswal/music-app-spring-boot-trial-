import React, { useState, useEffect } from 'react';
import { searchSongs, getRecentSongs } from './api';
import { Play, Search, Music, Disc, Plus, Home, Heart } from 'lucide-react';

function App() {
  const [query, setQuery] = useState('');
  const [songs, setSongs] = useState([]);
  const [currentSong, setCurrentSong] = useState(null);
  const [view, setView] = useState('home'); // Views: 'home', 'search', 'library'

  useEffect(() => {
    loadFeatured();
  }, []);

  // Home Page ke liye trending gaane
  const loadFeatured = async () => {
    try {
      const res = await getRecentSongs();
      setSongs(res.data.data);
      setView('home');
    } catch (e) { console.log("Recent songs load nahi huye"); }
  };

  // Search logic
  const handleSearch = async () => {
    if(!query) return;
    const res = await searchSongs(query);
    setSongs(res.data.data);
    setView('search');
  };

  // DATABASE se saved gaane lana (Day 12)
  const loadLibrary = async () => {
    try {
        // Hum Playlist ID 1 mangwa rahe hain kyunki usi mein 5 gaane hain
        const response = await fetch(`http://localhost:8080/api/playlists/1`);
        const result = await response.json();
        
        console.log("Backend Se Data Aaya:", result);

        // DHAYAN SE: Tere JSON mein data.songs ke andar gaane hain
        if (result.status === "success" && result.data && result.data.songs) {
            setSongs(result.data.songs); // Yahan fix hai!
            setView('library');
        } else {
            setSongs([]);
            setView('library');
            alert("Bhai, ye playlist toh khali nikli!");
        }
    } catch (error) {
        console.error("Library load error:", error);
        alert("Backend se baat nahi ho pa rahi!");
    }
};

  const playSong = (song) => {
    if (!song.audioUrl) {
        alert("Bhai, is gaane ka audio link database mein nahi hai!");
        return;
    }
    setCurrentSong(song);
    const audio = document.querySelector('audio');
    if (audio) {
        audio.src = song.audioUrl;
        audio.load();
        audio.play().catch(e => console.error("Playback failed:", e));
    }
  };
  const addToPlaylist = async (song) => {
    try {
        const response = await fetch(`http://localhost:8080/api/playlists/1/songs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(song) 
        });
        
        if(response.ok) {
            alert(`✅ ${song.title} Library mein add ho gaya!`);
            // AGAR TU LIBRARY VIEW MEIN HAI, TOH TURANT REFRESH KARO
            if (view === 'library') {
                loadLibrary(); 
            }
        }
    } catch (error) {
        console.error("Add error:", error);
    }
};
  return (
    <div style={styles.container}>
      {/* Sidebar */}
      <div style={styles.sidebar}>
        <h2 style={{color: '#1DB954', display:'flex', alignItems:'center', gap:'10px'}}><Music /> MusicApp</h2>
        <nav style={styles.nav}>
          <div style={{...styles.navItem, color: view === 'home' ? '#1DB954' : 'white'}} onClick={loadFeatured}>
            <Home size={22}/> Home
          </div>
          <div style={{...styles.navItem, color: view === 'search' ? '#1DB954' : 'white'}} onClick={() => setView('search')}>
            <Search size={22}/> Search
          </div>
          <div style={{...styles.navItem, color: view === 'library' ? '#1DB954' : 'white'}} onClick={loadLibrary}>
            <Heart size={22}/> Your Library
          </div>
        </nav>
      </div>

      {/* Main Area */}
      <div style={styles.main}>
        {view === 'search' && (
          <div style={styles.searchBar}>
            <input 
              type="text" 
              placeholder="Shakira, Arijit Singh..." 
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              style={styles.input}
            />
            <button onClick={handleSearch} style={styles.searchBtn}><Search size={20}/></button>
          </div>
        )}

        <h3>
          {view === 'home' && "Recommended for you"}
          {view === 'search' && `Results for "${query}"`}
          {view === 'library' && "Your Liked Songs"}
        </h3>

        <div style={styles.songGrid}>
          {songs.length > 0 ? songs.map((song, index) => (
            <div key={index} style={styles.card} onClick={() => playSong(song)}>
              <img src={song.albumArtUrl || 'https://via.placeholder.com/150'} style={styles.albumArt} alt="art" />
              <button onClick={(e) => { e.stopPropagation(); addToPlaylist(song); }} style={styles.addBtn}>
                <Plus size={18} color="black" />
              </button>
              <div style={styles.songTitle}>{song.title}</div>
              <div style={styles.songArtist}>{song.artist}</div>
            </div>
          )) : <p style={{color: '#b3b3b3'}}>Yahan kuch nahi hai bhai...</p>}
        </div>
      </div>

      {/* Player Bar */}
      {currentSong && (
        <div style={styles.playerBar}>
          <div style={{display:'flex', alignItems:'center', gap:'15px', width: '30%'}}>
             <img src={currentSong.albumArtUrl} style={{width:'55px', borderRadius:'4px'}} alt="thumb" />
             <div style={{overflow:'hidden'}}>
                <div style={{fontSize:'14px', fontWeight:'bold', whiteSpace:'nowrap'}}>{currentSong.title}</div>
                <div style={{fontSize:'12px', color:'#b3b3b3'}}>{currentSong.artist}</div>
             </div>
          </div>
          <div style={{width: '40%', textAlign: 'center'}}>
            <audio controls autoPlay src={currentSong.audioUrl} style={{width: '100%', height: '40px'}}></audio>
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
  navItem: { display: 'flex', gap: '15px', marginBottom: '25px', cursor: 'pointer', fontWeight: 'bold', fontSize: '16px', transition: '0.2s' },
  main: { flex: 1, backgroundColor: '#121212', padding: '30px', overflowY: 'auto', paddingBottom: '120px' },
  searchBar: { display: 'flex', gap: '10px', marginBottom: '40px' },
  input: { padding: '12px 20px', borderRadius: '25px', width: '100%', maxWidth: '400px', border: 'none', backgroundColor: '#242424', color: 'white' },
  searchBtn: { backgroundColor: '#1DB954', border: 'none', borderRadius: '50%', padding: '12px', cursor: 'pointer' },
  songGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '24px' },
  card: { backgroundColor: '#181818', padding: '16px', borderRadius: '8px', cursor: 'pointer', position: 'relative', transition: '0.3s' },
  albumArt: { width: '100%', borderRadius: '4px', marginBottom: '12px' },
  songTitle: { fontSize: '15px', fontWeight: 'bold', marginBottom: '5px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' },
  songArtist: { fontSize: '13px', color: '#b3b3b3' },
  addBtn: { position: 'absolute', top: '20px', right: '20px', backgroundColor: '#1DB954', border: 'none', borderRadius: '50%', width: '32px', height: '32px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  playerBar: { position: 'fixed', bottom: 0, left: 0, width: '100%', background: '#181818', padding: '15px 30px', borderTop: '1px solid #282828', display: 'flex', alignItems: 'center', justifyContent: 'space-between', zIndex: 100 }
};

export default App;