import React, { useState, useEffect, useCallback } from 'react';
import { searchSongs, getRecentSongs } from './api';
import { 
  Search, Music, Plus, Home, Heart, User, LogOut, Play, 
  ChevronLeft, Repeat, SkipForward, SkipBack, Share2, 
  AlignLeft, History, Trash2, Settings, Save, MoreVertical, Radio, Info, Monitor, Disc, UserCircle, Shuffle, Layout, MinusCircle
} from 'lucide-react';

function App() {
  const [query, setQuery] = useState('');
  const [songs, setSongs] = useState([]);
  const [currentSong, setCurrentSong] = useState(null);
  const [view, setView] = useState('home'); 
  const [stats, setStats] = useState({ totalSongs: 0, totalPlaylists: 0 });
  const [isLooping, setIsLooping] = useState('none'); 
  const [isShuffle, setIsShuffle] = useState(false);
  const [playHistory, setPlayHistory] = useState([]);
  const [menuOpen, setMenuOpen] = useState(null); 
  const [playlists, setPlaylists] = useState([]); 
  const [showPlaylistModal, setShowPlaylistModal] = useState(false);
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [activePlaylistId, setActivePlaylistId] = useState(null);
  const [loggedInUser, setLoggedInUser] = useState(null);
  const [userPlaylistId, setUserPlaylistId] = useState(null);
  const [isRegistering, setIsRegistering] = useState(false);
  
  // ✅ Auth Form state management
  const [authForm, setAuthForm] = useState({ username: '', password: '', email: '' });

  const loadFeatured = async () => {
    try {
      const res = await getRecentSongs();
      setSongs(res.data.data);
      setView('home');
    } catch (e) { console.log("Load error"); }
  };

  const fetchStats = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/auth/stats');
      const data = await res.json();
      setStats(data);
    } catch (e) { console.log("Stats error"); }
  };

  const fetchUserPlaylists = async (userId) => {
    try {
      const res = await fetch(`http://localhost:8080/api/playlists/user/${userId}/all`);
      const result = await res.json();
      if (result.status === "success") {
        setPlaylists(result.data);
        // ✅ Save playlists to THIS USER's localStorage key
        localStorage.setItem(`userPlaylists_${userId}`, JSON.stringify(result.data));
      }
    } catch (e) { console.log("PL error"); }
  };

  const ensurePlaylistExists = async (userId) => {
    try {
      const res = await fetch(`http://localhost:8080/api/playlists/user/${userId}`);
      const result = await res.json();
      if (result.status === "success" && result.data) {
        setUserPlaylistId(result.data.id);
        return result.data.id;
      }
    } catch (e) { console.log("PL check fail"); }
    return null;
  };

  useEffect(() => {
    loadFeatured();
    fetchStats();
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      const user = JSON.parse(savedUser);
      setLoggedInUser(user);
      ensurePlaylistExists(user.id);
      
      // ✅ Load THIS USER's history
      const savedHistory = localStorage.getItem(`musicHistory_${user.id}`);
      if (savedHistory) setPlayHistory(JSON.parse(savedHistory));
      
      // ✅ Load THIS USER's playlists from localStorage cache
      const localPL = localStorage.getItem(`userPlaylists_${user.id}`);
      if (localPL) setPlaylists(JSON.parse(localPL));
      
      // ✅ Also fetch fresh playlists from backend
      fetchUserPlaylists(user.id);
    }
  }, []);

  // ✅ Handle Input Change for all fields
  const handleAuthInputChange = (e) => {
    const { name, value } = e.target;
    setAuthForm(prev => ({ ...prev, [name]: value }));
  };

  // ✅ handleRegister with validation
  const handleRegister = async () => {
    if(!authForm.username || !authForm.email || !authForm.password) {
      return alert("Poora detail bhar!");
    }
    try {
      const res = await fetch(`http://localhost:8080/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(authForm)
      });
      const result = await res.json();
      if (res.ok) { 
        alert("Account ban gaya! Ab login kar le."); 
        setIsRegistering(false); 
        setAuthForm({ username: '', password: '', email: '' });
      } else { 
        alert(result.message || "Registration fail! Email already exists."); 
      }
    } catch (e) { alert("Backend is offline!"); }
  };

  const handleLogin = async () => {
    try {
      const res = await fetch(`http://localhost:8080/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          usernameOrEmail: authForm.username, 
          password: authForm.password 
        })
      });
      const result = await res.json();
      if (result.status === "success") {
        const user = result.data;
        
        // ✅ FIRST: Clear any leftover state from previous session
        setPlayHistory([]);
        setPlaylists([]);
        setUserPlaylistId(null);
        setActivePlaylistId(null);
        setCurrentSong(null);
        setView('home');
        
        // ✅ THEN: Set new user
        setLoggedInUser(user);
        localStorage.setItem('user', JSON.stringify(user));
        
        // ✅ Load THIS USER's data
        ensurePlaylistExists(user.id);
        fetchUserPlaylists(user.id);
        
        // ✅ Load THIS USER's history from THEIR localStorage
        const savedHistory = localStorage.getItem(`musicHistory_${user.id}`);
        if (savedHistory) {
          setPlayHistory(JSON.parse(savedHistory));
        } else {
          setPlayHistory([]); // Start fresh if no history
        }
        
        // ✅ Load THIS USER's cached playlists
        const cachedPlaylists = localStorage.getItem(`userPlaylists_${user.id}`);
        if (cachedPlaylists) {
          setPlaylists(JSON.parse(cachedPlaylists));
        }
        
      } else { alert(result.message || "User nahi mila ya password galat!"); }
    } catch (e) { alert("Backend band hai!"); }
  };

  // ✅ FIXED: Logout should ONLY clear current session, NOT delete user data
  const handleLogout = () => {
    // ✅ Only remove the current session (user object)
    localStorage.removeItem('user');
    
    // ✅ Reset state
    setLoggedInUser(null);
    setPlaylists([]);
    setPlayHistory([]);
    setUserPlaylistId(null);
    setActivePlaylistId(null);
    setCurrentSong(null);
    setView('home');
    
    window.location.reload(); 
  };

  const playSong = (song) => {
    setCurrentSong(song);
    setView('playing');
    const updatedHistory = [song, ...playHistory.filter(s => s.id !== song.id)].slice(0, 20);
    setPlayHistory(updatedHistory);
    
    // ✅ Save history to THIS USER's localStorage
    if(loggedInUser) {
      localStorage.setItem(`musicHistory_${loggedInUser.id}`, JSON.stringify(updatedHistory));
    }
    setMenuOpen(null);
  };

  const skipSong = useCallback((direction) => {
    const currentList = view === 'history' ? playHistory : songs;
    if (currentList.length === 0 || !currentSong) return;
    let nextIndex;
    const currentIndex = currentList.findIndex(s => s.id === currentSong.id);
    if (isShuffle && direction === 'next') nextIndex = Math.floor(Math.random() * currentList.length);
    else nextIndex = direction === 'next' ? currentIndex + 1 : currentIndex - 1;
    if (nextIndex >= currentList.length) { if (isLooping === 'all') nextIndex = 0; else return; }
    if (nextIndex < 0) nextIndex = currentList.length - 1;
    playSong(currentList[nextIndex]);
  }, [songs, playHistory, currentSong, isShuffle, isLooping, view]);

  const removeFromPlaylist = async (songId) => {
    if(!activePlaylistId) return;
    try {
      const res = await fetch(`http://localhost:8080/api/playlists/${activePlaylistId}/songs/${songId}`, { method: 'DELETE' });
      if (res.ok) {
        setSongs(songs.filter(s => s.id !== songId));
        setMenuOpen(null);
      }
    } catch (e) { alert("Remove error!"); }
  };

  const deletePlaylist = async (pId) => {
    if(!window.confirm("Bhai, pakka uda dun?")) return;
    try {
      const res = await fetch(`http://localhost:8080/api/playlists/${pId}`, { method: 'DELETE' });
      if (res.ok) {
        const updated = playlists.filter(p => p.id !== pId);
        setPlaylists(updated);
        // ✅ Update THIS USER's cached playlists
        localStorage.setItem(`userPlaylists_${loggedInUser.id}`, JSON.stringify(updated));
        if (activePlaylistId === pId) loadFeatured();
      }
    } catch (e) { alert("Error deleting!"); }
  };

  const handleSearch = async () => {
    if (!query) return;
    try {
      const res = await searchSongs(query);
      setSongs(res.data.data);
      setView('search');
    } catch (e) { alert("Search failed!"); }
  };

  const loadPlaylistSongs = async (pId) => {
    try {
      const res = await fetch(`http://localhost:8080/api/playlists/${pId}`);
      const result = await res.json();
      if (result.status === "success") {
        setSongs(result.data.songs || []);
        setView('playlist-view');
        setActivePlaylistId(pId);
        setMenuOpen(null);
      }
    } catch (e) { alert("Playlist empty!"); }
  };

  const createPlaylist = async () => {
    if(!newPlaylistName || !loggedInUser) return;
    try {
      const res = await fetch(`http://localhost:8080/api/playlists/create?name=${newPlaylistName}&userId=${loggedInUser.id}`, { method: 'POST' });
      const data = await res.json();
      if(data.status === "success") {
        const updated = [...playlists, data.data];
        setPlaylists(updated);
        // ✅ Save to THIS USER's localStorage
        localStorage.setItem(`userPlaylists_${loggedInUser.id}`, JSON.stringify(updated));
        setNewPlaylistName('');
        setShowPlaylistModal(false);
      }
    } catch (e) { console.log("Error"); }
  };

  const addToSpecificPlaylist = async (song, pId) => {
    try {
      const res = await fetch(`http://localhost:8080/api/playlists/${pId}/songs`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(song)
      });
      if (res.ok) { alert(`Saved! 🔥`); setMenuOpen(null); }
    } catch (error) { alert("Error adding song"); }
  };

  const getYouTubeId = (url) => {
    if (!url) return null;
    const match = url.match(/(?:youtu\.be\/|youtube\.com(?:\/embed\/|\/v\/|\/watch\?v=|\/watch\?.+&v=))([\w-]{11})/);
    return match ? match[1] : null;
  };

  if (!loggedInUser) {
    return (
      <div style={styles.authContainer}>
        <div style={styles.authCard}>
          <Music size={50} color="#1DB954" style={{ marginBottom: '20px' }} />
          <h2 style={{ marginBottom: '30px' }}>{isRegistering ? 'Sign up for free' : 'Log in to MusicApp'}</h2>
          
          <input name="username" style={styles.authInput} placeholder="Username" value={authForm.username} onChange={handleAuthInputChange} />
          {isRegistering && <input name="email" style={styles.authInput} placeholder="Email" value={authForm.email} onChange={handleAuthInputChange} />}
          <input name="password" style={styles.authInput} type="password" placeholder="Password" value={authForm.password} onChange={handleAuthInputChange} />
          
          <button style={styles.authBtn} onClick={isRegistering ? handleRegister : handleLogin}>
            {isRegistering ? 'Sign Up' : 'Log In'}
          </button>
          
          <p onClick={() => { setIsRegistering(!isRegistering); setAuthForm({username:'', email:'', password:''}); }} style={{ marginTop: '20px', fontSize: '14px', color: '#b3b3b3', cursor: 'pointer' }}>
            {isRegistering ? 'Already have an account? Log in' : "Don't have an account? Sign up"}
          </p>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.sidebar}>
        <h2 style={{ color: '#1DB954', cursor: 'pointer', display: 'flex', gap: '10px' }} onClick={loadFeatured}><Music /> MusicApp</h2>
        <nav style={styles.nav}>
          <div style={{...styles.navItem, color: view === 'home' ? '#1DB954' : 'white'}} onClick={loadFeatured}><Home size={22} /> Home</div>
          <div style={{...styles.navItem, color: view === 'search' ? '#1DB954' : 'white'}} onClick={() => setView('search')}><Search size={22} /> Search</div>
          <div style={{...styles.navItem, color: view === 'history' ? '#1DB954' : 'white'}} onClick={() => setView('history')}><History size={22} /> History</div>
          <div style={styles.navItem} onClick={handleLogout}><LogOut size={22} /> Logout</div>
          <hr style={{ borderColor: '#282828', margin: '20px 0' }} />
          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', paddingRight:'10px', marginBottom:'15px'}}>
             <span style={{fontSize:'12px', color:'#b3b3b3', letterSpacing:'1px', fontWeight:'bold'}}>PLAYLISTS</span>
             <Plus size={16} onClick={() => setShowPlaylistModal(true)} style={{cursor:'pointer'}}/>
          </div>
          <div style={styles.playlistScroll}>
             <div style={{...styles.playlistItem, color: activePlaylistId === userPlaylistId ? 'white' : '#b3b3b3'}} onClick={() => loadPlaylistSongs(userPlaylistId)}>
               <Heart size={16} /> Liked Songs
             </div>
             {playlists.map((p) => (
               <div key={p.id} style={{...styles.playlistItem, color: activePlaylistId === p.id ? 'white' : '#b3b3b3'}} onClick={() => loadPlaylistSongs(p.id)}>
                 <Disc size={16} /> {p.name}
                 <Trash2 size={14} style={{marginLeft:'auto', opacity:0.5}} onClick={(e)=>{e.stopPropagation(); deletePlaylist(p.id);}}/>
               </div>
             ))}
          </div>
        </nav>
      </div>

      <div style={styles.main}>
        {view !== 'playing' && (
          <div style={styles.searchBar}>
            <input style={styles.input} placeholder="Search songs..." value={query} onChange={(e) => setQuery(e.target.value)} onKeyPress={(e) => e.key === 'Enter' && handleSearch()} />
            <button onClick={handleSearch} style={styles.searchBtn}><Search size={20} /></button>
          </div>
        )}

        {(view === 'home' || view === 'search' || view === 'history' || view === 'playlist-view') && (
          <div style={styles.songGrid}>
            {(view === 'history' ? playHistory : songs).map((song, index) => (
              <div key={index} style={styles.card} onClick={() => playSong(song)}>
                <img src={song.albumArtUrl || 'https://via.placeholder.com/150'} style={styles.albumArt} alt="art" />
                <div style={styles.moreIcon} onClick={(e) => { e.stopPropagation(); setMenuOpen(menuOpen === index ? null : index); }}><MoreVertical size={20} /></div>
                {menuOpen === index && (
                    <div style={styles.contextMenu} onClick={(e) => e.stopPropagation()}>
                        <div style={styles.menuHeader}>Save to...</div>
                        <div style={styles.menuItem} onClick={() => addToSpecificPlaylist(song, userPlaylistId)}><Plus size={14}/> Liked</div>
                        {playlists.map(p => (
                            <div key={p.id} style={styles.menuItem} onClick={() => addToSpecificPlaylist(song, p.id)}><Disc size={14}/> {p.name}</div>
                        ))}
                        {view === 'playlist-view' && (
                          <div style={{...styles.menuItem, color:'#ff4d4d'}} onClick={() => removeFromPlaylist(song.id)}>
                            <MinusCircle size={14}/> Remove Song
                          </div>
                        )}
                    </div>
                )}
                <div style={{ fontWeight: 'bold', marginTop: '10px' }}>{song.title}</div>
                <div style={{ color: '#b3b3b3', fontSize: '12px' }}>{song.artist}</div>
              </div>
            ))}
          </div>
        )}

        {showPlaylistModal && (
          <div style={styles.modalOverlay}>
            <div style={styles.modal}>
              <h3>New Playlist</h3>
              <input style={styles.input} value={newPlaylistName} onChange={(e)=>setNewPlaylistName(e.target.value)} />
              <button onClick={createPlaylist} style={styles.saveBtn}>Create</button>
              <button onClick={()=>setShowPlaylistModal(false)} style={{...styles.saveBtn, backgroundColor:'#333'}}>Cancel</button>
            </div>
          </div>
        )}

        {view === 'playing' && currentSong && (
          <div style={styles.playingContainer}>
            <button onClick={() => setView('home')} style={styles.backBtn}><ChevronLeft /> Back</button>
            <div style={styles.playerLayout}>
              <div style={styles.videoSection}>
                <iframe width="100%" height="480" src={`https://www.youtube.com/embed/${getYouTubeId(currentSong.audioUrl)}?autoplay=1&loop=${isLooping === 'one' ? 1 : 0}&playlist=${getYouTubeId(currentSong.audioUrl)}`} frameBorder="0" allowFullScreen style={{ borderRadius: '15px' }}></iframe>
              </div>
              <div style={styles.detailsSection}>
                <img src={currentSong.albumArtUrl} style={styles.bigArt} alt="cover" />
                <h1 style={{marginTop: '20px'}}>{currentSong.title}</h1>
                <div style={styles.controlsRow}>
                  <button onClick={() => setIsShuffle(!isShuffle)} style={{...styles.iconBtn, color: isShuffle ? '#1DB954' : 'white'}}><Shuffle size={24} /></button>
                  <SkipBack size={32} fill="white" onClick={() => skipSong('prev')} style={{cursor:'pointer'}}/>
                  <div style={styles.playCircle}><Play size={24} fill="black" /></div>
                  <SkipForward size={32} fill="white" onClick={() => skipSong('next')} style={{cursor:'pointer'}}/>
                  <button onClick={() => setIsLooping(isLooping === 'none' ? 'all' : isLooping === 'all' ? 'one' : 'none')} style={{...styles.iconBtn, color: isLooping !== 'none' ? '#1DB954' : 'white'}}>
                    <Repeat size={24} />
                    {isLooping === 'one' && <span style={styles.repeatOneBadge}>1</span>}
                  </button>
                </div>
                <div style={styles.largeVisualizer}>
                  {[...Array(24)].map((_, i) => (
                    <div key={i} className="bar" style={{ width: '5px', background: '#1DB954', borderRadius: '3px', animation: `bounce 0.4s infinite alternate ${i * 0.04}s` }} />
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
      <style>{`
        @keyframes bounce { from { height: 6px; } to { height: 40px; } }
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
      `}</style>
    </div>
  );
}

const styles = {
  authContainer: { height: '100vh', width: '100vw', background: 'linear-gradient(#1a1a1a, #000)', display: 'flex', justifyContent: 'center', alignItems: 'center' },
  authCard: { background: '#121212', padding: '50px', borderRadius: '12px', textAlign: 'center', width: '420px', boxShadow: '0 20px 50px rgba(0,0,0,0.7)' },
  authInput: { width: '100%', padding: '14px', borderRadius: '4px', background: '#333', border: 'none', color: '#fff', marginBottom: '15px', outline: 'none' },
  authBtn: { width: '100%', background: '#1DB954', color: '#000', border: 'none', padding: '14px', borderRadius: '30px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' },
  container: { backgroundColor: '#000', color: 'white', minHeight: '100vh', display: 'flex', fontFamily: 'sans-serif' },
  sidebar: { width: '260px', padding: '24px', borderRight: '1px solid #333', background: '#000' },
  navItem: { display: 'flex', gap: '15px', marginBottom: '20px', cursor: 'pointer', alignItems: 'center', fontWeight: 'bold', fontSize: '14px' },
  main: { flex: 1, backgroundColor: '#121212', padding: '30px', overflowY: 'auto' },
  songGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '25px' },
  card: { backgroundColor: '#181818', padding: '15px', borderRadius: '10px', cursor: 'pointer', position: 'relative' },
  albumArt: { width: '100%', borderRadius: '8px' },
  moreIcon: { position: 'absolute', top: '20px', right: '20px', background: 'rgba(0,0,0,0.6)', borderRadius: '50%', padding: '5px' },
  contextMenu: { position: 'absolute', top: '55px', right: '10px', backgroundColor: '#282828', borderRadius: '4px', padding: '8px', zIndex: 100, width: '200px', boxShadow: '0 16px 24px rgba(0,0,0,0.5)' },
  menuHeader: { padding:'5px', fontSize:'11px', color:'#b3b3b3', fontWeight:'bold', textTransform:'uppercase' },
  menuItem: { padding: '10px', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '10px', color: '#b3b3b3', cursor: 'pointer' },
  modalOverlay: { position:'fixed', top:0, left:0, width:'100%', height:'100%', backgroundColor:'rgba(0,0,0,0.8)', display:'flex', justifyContent:'center', alignItems:'center', zIndex:1000 },
  modal: { backgroundColor:'#282828', padding:'40px', borderRadius:'15px', width:'400px', textAlign:'center' },
  saveBtn: { backgroundColor: '#1DB954', color: 'white', border: 'none', padding: '12px 25px', borderRadius: '30px', fontWeight: 'bold', cursor: 'pointer' },
  playingContainer: { animation: 'fadeIn 0.6s ease' },
  backBtn: { background: 'none', border: '1px solid #333', color: '#fff', padding: '8px 20px', borderRadius: '20px', cursor: 'pointer', marginBottom: '20px' },
  playerLayout: { display: 'flex', gap: '40px' },
  videoSection: { flex: 2 },
  detailsSection: { flex: 1, textAlign: 'center' },
  bigArt: { width: '300px', height: '300px', borderRadius: '15px', boxShadow: '0 15px 40px rgba(0,0,0,0.8)' },
  controlsRow: { display: 'flex', gap: '25px', marginTop: '30px', justifyContent: 'center', alignItems: 'center' },
  playCircle: { background: 'white', width:'56px', height:'56px', borderRadius:'50%', display:'flex', justifyContent:'center', alignItems:'center' },
  iconBtn: { background: 'none', border: 'none', cursor: 'pointer', position: 'relative' },
  repeatOneBadge: { position:'absolute', top:'-5px', right:'-5px', background:'#1DB954', color:'black', fontSize:'10px', borderRadius:'50%', width:'14px', height:'14px', fontWeight:'bold' },
  largeVisualizer: { display: 'flex', gap: '4px', height: '40px', justifyContent: 'center', alignItems: 'flex-end', marginTop: '30px' },
  input: { flex: 1, padding: '12px 20px', borderRadius: '30px', border: 'none', backgroundColor: '#333', color: 'white', width: '100%' },
  searchBar: { display: 'flex', gap: '10px', marginBottom: '30px', maxWidth: '500px' },
  searchBtn: { backgroundColor: '#1DB954', border: 'none', borderRadius: '50%', padding: '10px', cursor: 'pointer' },
  playlistScroll: { maxHeight:'450px', overflowY:'auto' },
  playlistItem: { padding:'12px 0', fontSize:'14px', cursor:'pointer', display:'flex', alignItems:'center', gap:'12px' },
};

export default App;