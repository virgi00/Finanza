// src/pages/Dashboard.tsx
import React, { useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { API_BASE_URL } from '../config/api';
import '../App.css';

interface CreateArticlePayload {
  title: string;
  body: string;
  date: string;      // formato YYYY-MM-DD
  srcImg?: string | null; // Data URL (base64) oppure null
}

const DashboardPage: React.FC = () => {
  const [text, setText] = useState('');
  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [tags, setTags] = useState<string[]>([]);
  const [manualTags, setManualTags] = useState<string[]>([]);
  const [currentTag, setCurrentTag] = useState('');
  const [image, setImage] = useState<File | null>(null);
  const [imageDataUrl, setImageDataUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [publishing, setPublishing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    // Trigger custom event to notify other components
    window.dispatchEvent(new Event('authChanged'));
    navigate('/');
  };

  // --- Helpers ---
  const fileToDataUrl = (file: File): Promise<string> =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(typeof reader.result === 'string' ? reader.result : '');
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });

  // --- Gestione tag manuali ---
  const addManualTag = () => {
    const tag = currentTag.trim();
    if (tag && !manualTags.includes(tag)) {
      setManualTags(prev => [...prev, tag]);
      setCurrentTag('');
    }
  };

  const removeManualTag = (tagToRemove: string) => {
    setManualTags(prev => prev.filter(tag => tag !== tagToRemove));
  };

  const handleTagKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      addManualTag();
    }
  };

  // --- Bottoni azione ---
  const handleGenerate = async () => {
    const token = localStorage.getItem('authToken');
    setError(null);
    setLoading(true);

    const deriveTitle = (s: string) => {
      if (!s) return '';
      const firstSentence = s.split(/(?<=[.!?])\s+/)[0];
      if (firstSentence && firstSentence.length <= 120) return firstSentence;
      return s.split(/\s+/).slice(0, 12).join(' ') + '…';
    };

    try {
      const res = await fetch(`${API_BASE_URL}/api/summarize`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({ text, wordCount: 160 }),
      });

      if (!res.ok) {
        const serverMsg =
          (await res.text().catch(() => '')) || 'Errore nella generazione automatica';
        throw new Error(serverMsg);
      }

      const data = await res.json();
      const rawSummary: string = data?.summary || '';
      const newTags: string[] = Array.isArray(data?.tags) ? data.tags : [];
      const newTitle: string = data?.title || deriveTitle(rawSummary);

      // Pulisce il summary dai metadata
      let cleanSummary = rawSummary;
      cleanSummary = cleanSummary.replace(/Tags:.*$/gm, ''); // Rimuove righe con Tags:
      cleanSummary = cleanSummary.replace(/Category:.*$/gm, ''); // Rimuove righe con Category:
      cleanSummary = cleanSummary.replace(/#\w+/g, ''); // Rimuove hashtag
      cleanSummary = cleanSummary.replace(/\n\s*\n/g, '\n'); // Rimuove righe vuote multiple
      cleanSummary = cleanSummary.trim();

      setTitle(newTitle);
      setSummary(cleanSummary);
      // Aggiungi i tag generati dall'AI ai tag manuali invece di tenerli separati
      if (newTags.length > 0) {
        const uniqueTags = Array.from(new Set([...manualTags, ...newTags]));
        setManualTags(uniqueTags);
      }
      setTags([]); // Reset dei tag automatici dato che li abbiamo spostati nei manuali
    } catch (err: any) {
      setError(err?.message || 'Errore sconosciuto');
    } finally {
      setLoading(false);
    }
  };

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setImage(file);
    try {
      const dataUrl = await fileToDataUrl(file);
      setImageDataUrl(dataUrl);
    } catch {
      setImageDataUrl(null);
    }
  };

  const handleImageButtonClick = () => {
    fileInputRef.current?.click();
  };

  const handlePublish = async () => {
    if (!title || !summary) {
      alert('Inserisci almeno titolo e riassunto prima di pubblicare!');
      return;
    }

    setPublishing(true);
    setError(null);

    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    const isoDate = `${yyyy}-${mm}-${dd}`;

    const payload: CreateArticlePayload = {
      title: title.trim(),
      body: (summary || text).trim(),
      date: isoDate,
      srcImg: imageDataUrl || null,
    };

    // Aggiungi i tag manuali (che ora includono sia quelli generati che quelli aggiunti manualmente)
    if (manualTags.length > 0) {
      (payload as any).tags = manualTags;
    }

    try {
      const token = localStorage.getItem('authToken');
      console.log('[Dashboard] Publishing article:', payload);
      
      const res = await fetch(`${API_BASE_URL}/articles/newArticle/`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const msgText = await res.text().catch(() => '');
        console.error(`[Dashboard] Error response (${res.status}):`, msgText);
        throw new Error(msgText || `Errore HTTP ${res.status} nella creazione dell'articolo`);
      }

      const created = await res.json().catch(() => null);
      console.log('[Dashboard] Articolo creato:', created);
      alert('Articolo pubblicato con successo!');

      // Notifica la Home e reindirizza
      window.dispatchEvent(new Event('articlesChanged'));
      navigate('/home');

      // Reset form
      setText('');
      setTitle('');
      setSummary('');
      setTags([]);
      setManualTags([]);
      setCurrentTag('');
      setImage(null);
      setImageDataUrl(null);
    } catch (err: any) {
      console.error('[Dashboard] Error publishing article:', err);
      setError(err.message || 'Errore durante la pubblicazione');
      alert(err.message || 'Errore durante la pubblicazione');
    } finally {
      setPublishing(false);
    }
  };

  return (
    <>
      <nav className="top-nav">
        <Link to="/" className="brand">Finanza</Link>
        <div className="nav-right">
          <button onClick={handleLogout} className="nav-btn logout-btn">Logout</button>
          <Link to="/dashboard" className="nav-btn">Dashboard</Link>
        </div>
      </nav>
      <div style={{ height: '60px' }} />

      <div className="dashboard-container">
        <h2>Inserisci una nuova notizia finanziaria</h2>

        {error && <div className="error-message">{error}</div>}

        <textarea
          className="input-text"
          rows={5}
          placeholder="Inserisci il testo della notizia (anche parziale)..."
          value={text}
          onChange={(e) => setText(e.target.value)}
        />

        <div style={{ display: 'flex', gap: 12, margin: '8px 0 4px' }}>
          <button type="button" className="btn-image" onClick={handleImageButtonClick}>
            Scegli immagine
          </button>
          <button
            className="btn-generate"
            onClick={handleGenerate}
            disabled={loading || !text}
          >
            {loading ? 'Generazione in corso...' : 'Genera titolo, riassunto e tag'}
          </button>
        </div>

        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          onChange={handleImageChange}
          style={{ display: 'none' }}
        />

        {/* Campo per i tag sempre visibile */}
        <div className="tags-input-section">
          <label>Tag articolo</label>
          <div className="manual-tags-section">
            <div style={{ marginBottom: '12px' }}>
              <input
                className="tag-input"
                type="text"
                placeholder="Scrivi un tag e premi Invio..."
                value={currentTag}
                onChange={(e) => {
                  console.log('Tag input changed:', e.target.value);
                  setCurrentTag(e.target.value);
                }}
                onKeyPress={handleTagKeyPress}
                onFocus={() => console.log('Tag input focused')}
                onBlur={() => console.log('Tag input blurred')}
                onClick={() => console.log('Tag input clicked')}
                disabled={false}
                autoComplete="off"
                tabIndex={0}
              />
              <button 
                type="button"
                className="btn-add-tag"
                onClick={addManualTag}
                disabled={!currentTag.trim()}
                style={{ marginLeft: '8px', verticalAlign: 'top' }}
              >
                Aggiungi
              </button>
            </div>
            
            {manualTags.length > 0 && (
              <div className="tags-container">
                {manualTags.map((tag, i) => (
                  <span className="tag manual-tag" key={i}>
                    #{tag}
                    <button 
                      type="button"
                      className="remove-tag"
                      onClick={() => removeManualTag(tag)}
                      aria-label={`Rimuovi tag ${tag}`}
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>

        {(title || summary) && (
          <div className="generated-section">
            <label>Titolo</label>
            <input
              className="input-field"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />

            <label>Riassunto (200–600 caratteri)</label>
            <textarea
              className="input-text"
              rows={4}
              value={summary}
              onChange={(e) => setSummary(e.target.value)}
              required
            />

            {imageDataUrl && (
              <>
                <label>Anteprima immagine</label>
                <img
                  src={imageDataUrl}
                  alt="Anteprima"
                  className="preview-image"
                />
              </>
            )}

            <button
              className="btn-publish"
              onClick={handlePublish}
              disabled={publishing}
            >
              {publishing ? 'Pubblicazione in corso…' : 'Pubblica Notizia'}
            </button>
          </div>
        )}
      </div>
    </>
  );
};

export default DashboardPage;