import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { API_BASE_URL } from '../config/api';

const LoginPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const res = await fetch(`${API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        const msg = (data && (data.message || data.error)) || "Login fallito";
        throw new Error(msg);
      }

      // Salva lo stato di autenticazione con le chiavi lette da App.tsx
      localStorage.setItem("authToken", data.token ?? "ok");
      localStorage.setItem("username", data.username ?? username);

      // Notifica l'app che lo stato auth è cambiato
      window.dispatchEvent(new Event("authChanged"));

      // Vai in dashboard
      navigate("/dashboard");
    } catch (err: any) {
      alert(err?.message || "Errore durante il login");
    }
  };

  return (
    <>
      <div style={{ height: '60px' }}></div>
      <div className="login-container">
        <form className="login-card" onSubmit={handleLogin}>
          <h2>Login</h2>
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Username"
            autoComplete="username"
            required
          />
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Password"
            autoComplete="current-password"
            required
          />
          <button type="submit">Login</button>
          <div style={{ marginTop: '12px', textAlign: 'center' }}>
            <span>Non hai un account? </span>
            <Link to="/register" className="nav-btn" style={{ padding: '4px 10px', borderRadius: '14px' }}>
              Registrati
            </Link>
          </div>
        </form>
      </div>
    </>
  );
};

export default LoginPage;