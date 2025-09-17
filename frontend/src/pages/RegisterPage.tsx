import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { API_BASE_URL } from '../config/api';

const RegisterPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail]     = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm]   = useState('');
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();

    if (password !== confirm) {
      alert('Le password non coincidono');
      return;
    }

    try {
      const res = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, email, password }),
      });

      if (!res.ok) {
        throw new Error('Registrazione fallita');
      }

      // opzionale: login automatico dopo registrazione
      // const data = await res.json();
      // localStorage.setItem('token', data.token);

      alert('Registrazione completata!');
      navigate('/login');
    } catch (err) {
      alert('Errore durante la registrazione');
    }
  };

  return (
    <>
      <nav className="top-nav">
        <Link to="/" className="brand">Finanza</Link>
        <div className="nav-right">
          <Link to="/login" className="nav-btn">Login</Link>
        </div>
      </nav>
      <div style={{ height: '60px' }}></div>

      <div className="login-container">
        <form className="login-card" onSubmit={handleRegister}>
          <h2>Registrazione</h2>
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Username"
            autoComplete="username"
            required
          />
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Email"
            autoComplete="email"
            required
          />
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Password"
            autoComplete="new-password"
            required
          />
          <input
            type="password"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            placeholder="Conferma password"
            autoComplete="new-password"
            required
          />
          <button type="submit">Crea account</button>

          <div style={{ marginTop: 12, textAlign: 'center' }}>
            <span>Hai già un account? </span>
            <Link to="/login" className="nav-btn" style={{ padding: '4px 10px', borderRadius: '14px' }}>
              Accedi
            </Link>
          </div>
        </form>
      </div>
    </>
  );
};

export default RegisterPage;