import React, { useState, useEffect } from 'react';
import { searchSongs, getRecentSongs } from './api';
import { Search, Music, Plus, Home, Heart, User, LogOut, Play } from 'lucide-react';

function App() {
  const [query, setQuery] = useState('');
  const [songs, setSongs] = useState([]);
  const [currentSong, setCurrentSong] = useState(null);
  const [view, setView] = useState('home');
  const [stats, setStats] = useState({ totalSongs: 0, totalPlaylists: 0, totalUsers: 0 });

  const [isLoginOpen, setIsLoginOpen] = useState(false);
  const [authMode, setAuthMode] = useState('login'); 
  const [loggedInUser, setLoggedInUser] = useState(null);
  const [userPlaylistId, setUserPlaylistId] = useState(null); 
  const [authForm, setAuthForm] = useState({ username: '', password: '', email: '' });

  useEffect(() => {
    loadFeatured();
    fetchStats();
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
        const user = JSON.parse(savedUser);
        setLoggedInUser(user);
        ensurePlaylistExists(user.id);
    }
  }, []);

  const fetchStats = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/auth/stats');
      const data = await res.json();
      setStats(data);
    } catch (e) { console.log("Stats error"); }
  };

  const ensurePlaylistExists = async (userId) => {
    try {
        const res = await fetch(`http://localhost:8080/api/playlists/user/${userId}`);
        const result = await res.json();
        if(result.status === "success" && result.data) {
            setUserPlaylistId(result.data.id);
            return result.data.id;
        } else {
            const createRes = await fetch(`http://localhost:8080/api/playlists/create?name=My Favorites&userId=${userId}`, {
                method: 'POST'
            });
            const createData = await createRes.json();
            if(createData.status === "success") {
                setUserPlaylistId(createData.data.id);
                return createData.data.id;
            }
        }
    } catch (e) { console.log("Playlist check failed"); }
    return null;
  };

  const loadFeatured = async () => {
    try {
      const res = await getRecentSongs();
      setSongs(res.data.data);
      setView('home');
    } catch (e) { console.log("Load error"); }
  };

  const handleSearch = async () => {
    if(!query) return;
    try {
        const res = await searchSongs(query);
        setSongs(res.data.data);
        setView('search');
    } catch (e) { alert("Search failed!"); }
  };

  const loadLibrary = async () => {
    if (!loggedInUser) return setIsLoginOpen(true);
    let pid = userPlaylistId || await ensurePlaylistExists(loggedInUser.id);
    try {
        const response = await fetch(`http://localhost:8080/api/playlists/${pid}`);
        const result = await response.json();
        if (result.status === "success" && result.data) {
            setSongs(result.data.songs || []);
            setView('library');
        }
    } catch (error) { alert("Library error"); }
  };

  const handleLogin = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usernameOrEmail: authForm.username, password: authForm.password })
        });
        const result = await response.json();
        if (response.ok) {
            setLoggedInUser(result.data);
            localStorage.setItem('user', JSON.stringify(result.data)); 
            setIsLoginOpen(false);
            ensurePlaylistExists(result.data.id);
            alert("Login Success!");
        } else { alert("Login Fail: " + result.message); }
    } catch (error) { alert("Login failed"); }
  };

  const handleRegister = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                username: authForm.username, 
                email: authForm.email, 
                password: authForm.password 
            })
        });
        if (response.ok) { 
            alert("Registration Successful! Now Login."); 
            setAuthMode('login'); 
        } else { alert("Registration failed"); }
    } catch (error) { alert("Error connecting to backend"); }
  };

  const addToPlaylist = async (song) => {
    if (!loggedInUser) return setIsLoginOpen(true);
    let pid = userPlaylistId || await ensurePlaylistExists(loggedInUser.id);
    try {
        const response = await fetch(`http://localhost:8080/api/playlists/${pid}/songs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(song) 
        });
        if(response.ok) alert(`Added to My Favorites!`);
    } catch (error) { alert("Add failed"); }
  };

  const getYouTubeId = (url) => {
    if(!url) return null;
    const match = url.match(/(?:youtu\.be\/|youtube\.com(?:\/embed\/|\/v\/|\/watch\?v=|\/watch\?.+&v=))([\w-]{11})/);
    return match ? match[1] : null;
  };

  return (
    <div style={styles.container}>
      {/* Sidebar */}
      <div style={styles.sidebar}>
        <h2 style={{color: '#1DB954', cursor:'pointer', display:'flex', gap:'10px'}} onClick={loadFeatured}><Music /> MusicApp</h2>
        <nav style={styles.nav}>
          <div style={{...styles.navItem, color: view === 'home' ? '#1DB954' : 'white'}} onClick={loadFeatured}><Home size={22}/> Home</div>
          <div style={{...styles.navItem, color: view === 'search' ? '#1DB954' : 'white'}} onClick={() => setView('search')}><Search size={22}/> Search</div>
          <div style={{...styles.navItem, color: view === 'library' ? '#1DB954' : 'white'}} onClick={loadLibrary}><Heart size={22}/> Library</div>
          <hr style={{borderColor: '#282828', margin: '20px 0'}} />
          
          {!loggedInUser ? (
            <div style={styles.navItem} onClick={() => { setAuthMode('login'); setIsLoginOpen(true); }}><User size={22}/> Login / Register</div>
          ) : (
            <div>
                <div style={{color:'#1DB954', marginBottom:'15px'}}>Hi, {loggedInUser.username}</div>
                <div style={styles.navItem} onClick={() => {
                    localStorage.removeItem('user');
                    setLoggedInUser(null);
                    setUserPlaylistId(null);
                    window.location.reload();
                }}><LogOut size={20}/> Logout</div>
            </div>
          )}
        </nav>
        <div style={styles.statsCard}>🎵 {stats.totalSongs} | 📂 {stats.totalPlaylists}</div>
      </div>

      {/* Main Area */}
      <div style={styles.main}>
        <div style={styles.searchBar}>
            <input 
              style={styles.input} type="text" placeholder="Search for David Tavare..." 
              value={query} onChange={(e) => setQuery(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            />
            <button onClick={handleSearch} style={styles.searchBtn}><Search size={20}/></button>
        </div>

        <h3 style={{marginBottom:'20px'}}>{view === 'home' ? "Trending" : view === 'library' ? "My Favorites" : "Search Results"}</h3>
        
        <div style={styles.songGrid}>
          {songs.map((song, index) => (
            <div key={index} style={styles.card} onClick={() => setCurrentSong(song)}>
              <img src={song.albumArtUrl || 'https://via.placeholder.com/150'} style={styles.albumArt} alt="art" />
              <button onClick={(e) => { e.stopPropagation(); addToPlaylist(song); }} style={styles.addBtn}><Plus size={20}/></button>
              <div style={{fontWeight:'bold', marginTop:'10px'}}>{song.title}</div>
              <div style={{color:'#b3b3b3', fontSize:'12px'}}>{song.artist}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Modal - Unified Login/Register */}
      {isLoginOpen && (
        <div style={styles.modalOverlay}>
          <div style={styles.loginBox}>
            <h2 style={{color: '#1DB954', marginBottom: '20px'}}>{authMode === 'login' ? 'Login' : 'Register'}</h2>
            
            {authMode === 'register' && (
                <input style={styles.loginInput} placeholder="Email" onChange={(e) => setAuthForm({...authForm, email: e.target.value})} />
            )}
            <input style={styles.loginInput} placeholder="Username" onChange={(e) => setAuthForm({...authForm, username: e.target.value})} />
            <input style={styles.loginInput} type="password" placeholder="Password" onChange={(e) => setAuthForm({...authForm, password: e.target.value})} />
            
            <button style={styles.loginSubmit} onClick={authMode === 'login' ? handleLogin : handleRegister}>
                {authMode === 'login' ? 'Login' : 'Create Account'}
            </button>
            
            <p style={{marginTop: '15px', fontSize:'13px', cursor:'pointer'}} onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}>
                {authMode === 'login' ? "Don't have an account? Register" : "Already have an account? Login"}
            </p>
            
            <button onClick={() => setIsLoginOpen(false)} style={{marginTop:'10px', background:'none', border:'none', color:'#ccc', cursor:'pointer'}}>Cancel</button>
          </div>
        </div>
      )}

      {/* Player Bar */}
      {currentSong && (
        <div style={styles.playerBar}>
           <div style={{width:'30%', display:'flex', alignItems:'center', gap:'10px'}}>
              <img src={currentSong.albumArtUrl} style={{width:'50px', borderRadius:'4px'}} alt="thumb" />
              <div style={{fontSize:'14px', fontWeight:'bold'}}>{currentSong.title}</div>
           </div>
           
           <div style={{width:'40%', textAlign:'center'}}>
              <iframe 
                width="100%" height="80" 
                src={`https://www.youtube.com/embed/${getYouTubeId(currentSong.audioUrl)}?autoplay=1&origin=http://localhost:3000`}
                frameBorder="0" allow="autoplay; encrypted-media" title="player"
                referrerPolicy="strict-origin-when-cross-origin"
                style={{borderRadius: '8px'}}
              ></iframe>
           </div>
           <div style={{width:'30%', textAlign:'right', color:'#1DB954'}}><Play size={18} fill="#1DB954"/> AUDIO LIVE</div>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: { backgroundColor: '#000', color: 'white', minHeight: '100vh', display: 'flex' },
  sidebar: { width: '240px', padding: '24px', borderRight: '1px solid #333', display:'flex', flexDirection:'column' },
  nav: { marginTop: '30px' },
  navItem: { display: 'flex', gap: '15px', marginBottom: '20px', cursor: 'pointer', alignItems: 'center' },
  statsCard: { marginTop: 'auto', padding: '15px', background: '#121212', borderRadius: '8px', fontSize: '13px' },
  main: { flex: 1, backgroundColor: '#121212', padding: '30px', overflowY: 'auto', paddingBottom: '120px' },
  searchBar: { display: 'flex', gap: '10px', marginBottom: '30px', maxWidth: '500px' },
  input: { flex: 1, padding: '12px 20px', borderRadius: '30px', border: 'none', backgroundColor: '#333', color: 'white' },
  searchBtn: { backgroundColor: '#1DB954', border: 'none', borderRadius: '50%', padding: '10px', cursor: 'pointer' },
  songGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '20px' },
  card: { backgroundColor: '#181818', padding: '15px', borderRadius: '8px', cursor: 'pointer', position: 'relative' },
  albumArt: { width: '100%', borderRadius: '4px' },
  addBtn: { position: 'absolute', top: '15px', right: '15px', backgroundColor: '#1DB954', border: 'none', borderRadius: '50%', width: '35px', height: '35px', cursor: 'pointer', display:'flex', alignItems:'center', justifyContent:'center' },
  playerBar: { position: 'fixed', bottom: 0, left: 0, width: '100%', background: '#222', padding: '10px 20px', display: 'flex', alignItems: 'center', zIndex: 100, borderTop: '1px solid #333' },
  modalOverlay: { position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.8)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 2000 },
  loginBox: { backgroundColor: '#222', padding: '30px', borderRadius: '10px', width: '320px', display:'flex', flexDirection:'column' },
  loginInput: { padding: '10px', marginBottom: '15px', borderRadius: '5px', border: 'none', backgroundColor:'#333', color:'white' },
  loginSubmit: { padding: '10px', backgroundColor: '#1DB954', border: 'none', borderRadius: '20px', fontWeight: 'bold', cursor:'pointer' }
};

export default App;