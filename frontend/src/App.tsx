import './App.css';
import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Link, useLocation, useNavigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import HomePage from './pages/Home';
import RegisterPage from './pages/RegisterPage';
import Home from './pages/Home';
import DashboardPage from './pages/Dashboard';
import ArticlePage from './pages/ArticlePage';
import PrivateRoute from './routes/PrivateRoute';

function NavBar({ isLoggedIn, onLogout }: { isLoggedIn: boolean; onLogout: () => void }) {
  const location = useLocation();
  const navigate = useNavigate();
  const showLoginIcon = location.pathname !== '/login' && location.pathname !== '/register';
  
  const handleLogout = () => {
    onLogout();
    navigate('/'); // Redirect to home after logout
  };
  
  return (
    <nav className="top-nav">
      <Link to="/" className="brand">Finanza</Link>
      <div className="nav-right">
        <Link to="/home" className="nav-btn">Home</Link>
        {isLoggedIn && <Link to="/dashboard" className="nav-btn">Dashboard</Link>}
        {isLoggedIn ? (
          <button onClick={handleLogout} className="nav-btn logout-btn" title="Logout">
            Logout
          </button>
        ) : (
          showLoginIcon && (
            <Link to="/login" className="login-icon" aria-label="Accedi" title="Accedi">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M15.75 7.5a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0Z" stroke="currentColor" strokeWidth="1.8"/>
                <path d="M4.5 19.5a9 9 0 0 1 15 0" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
              </svg>
            </Link>
          )
        )}
      </div>
    </nav>
  );
}

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(
    Boolean(localStorage.getItem('authToken') || localStorage.getItem('username'))
  );

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    setIsLoggedIn(false);
    // Trigger custom event to notify other components
    window.dispatchEvent(new Event('authChanged'));
  };

  useEffect(() => {
    const checkAuth = () => {
      setIsLoggedIn(Boolean(localStorage.getItem('authToken') || localStorage.getItem('username')));
    };
    window.addEventListener('storage', checkAuth);
    window.addEventListener('authChanged', checkAuth as EventListener);
    return () => {
      window.removeEventListener('storage', checkAuth);
      window.removeEventListener('authChanged', checkAuth as EventListener);
    };
  }, []);

  return (
    <BrowserRouter>
      <NavBar isLoggedIn={isLoggedIn} onLogout={handleLogout} />
      <Routes>
        {/* Home = pagina pubblica */}
        <Route path="/" element={<Home />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/article/:id" element={<ArticlePage />} />
        <Route path="/dashboard" element={
          <PrivateRoute>
            <DashboardPage />
          </PrivateRoute>
        } />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="*" element={<Home />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;