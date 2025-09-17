// src/pages/Home.tsx
import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { API_BASE_URL } from '../config/api';

type ID = string | number;

interface NewsItem {
  id: ID;
  title: string;
  summary?: string;
  imageUrl?: string;
  tags?: string[];
  createdAt?: string;
}

// Tipi dal backend (Article con possibili varianti nei campi)
type BackendTag = string | { name?: string; tagName?: string };
interface BackendArticle {
  id: string | number;
  title: string;
  body?: string;
  date?: string;
  srcImg?: string;
  tags?: BackendTag[];
}

// Converte Article (backend) in NewsItem (frontend)
const mapArticleToNewsItem = (a: BackendArticle): NewsItem => {
  const tagList: string[] = (a.tags || []).map((t: BackendTag) => {
    if (typeof t === 'string') return t;
    return t.name || (t as any).tagName || '';
  }).filter(Boolean);

  // Pulisce il body dai metadata dei tag e categoria
  let cleanBody = a.body || '';
  cleanBody = cleanBody.replace(/Tags:.*$/gm, ''); // Rimuove righe con Tags:
  cleanBody = cleanBody.replace(/Category:.*$/gm, ''); // Rimuove righe con Category:
  cleanBody = cleanBody.replace(/#\w+/g, ''); // Rimuove hashtag
  cleanBody = cleanBody.replace(/\n\s*\n/g, '\n'); // Rimuove righe vuote multiple
  cleanBody = cleanBody.trim();

  // Limita il summary a circa 150 caratteri per l'anteprima
  const maxLength = 150;
  let summary = cleanBody;
  if (summary.length > maxLength) {
    summary = summary.substring(0, maxLength);
    // Trova l'ultima parola completa
    const lastSpace = summary.lastIndexOf(' ');
    if (lastSpace > 100) { // Solo se non taglia troppo
      summary = summary.substring(0, lastSpace);
    }
    summary += '...';
  }

  return {
    id: a.id,
    title: a.title,
    summary: summary,
    imageUrl: a.srcImg,
    createdAt: a.date,
    tags: tagList,
  };
};

const Home: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [items, setItems] = useState<NewsItem[]>([]);
  const [allItems, setAllItems] = useState<NewsItem[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState<number>(0);
  const pageSize = 8; // elementi per pagina

  // Get tag filter from URL parameters
  const tagFilter = searchParams.get('tag');

  const handleArticleClick = (articleId: ID) => {
    navigate(`/article/${articleId}`);
  };

  const fetchNews = useCallback(async (): Promise<void> => {
    try {
      setLoading(true);
      setError(null);

      let url = `${API_BASE_URL}/articles/all`;
      
      // If there's a tag filter, use the search by tag endpoint
      if (tagFilter) {
        url = `${API_BASE_URL}/articles/searchByTag/${encodeURIComponent(tagFilter)}`;
      }

      console.log(`[Home] Fetching news from: ${url}`);
      const res = await fetch(url);
      
      if (!res.ok) {
        const errorText = await res.text().catch(() => '');
        console.error(`[Home] Error response (${res.status}):`, errorText);
        throw new Error(errorText || `Errore HTTP ${res.status}`);
      }
      
      const data: unknown = await res.json();
      const arr = Array.isArray(data) ? (data as BackendArticle[]) : [];
      console.log(`[Home] Received ${arr.length} articles`);
      
      const mapped = arr.map(mapArticleToNewsItem);

      setAllItems(mapped);
      setPage(0);
      setItems(mapped.slice(0, pageSize));
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Errore sconosciuto';
      console.error('[Home] Error fetching news:', e);
      setError(msg);
      setAllItems([]);
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [pageSize, tagFilter]);

  useEffect(() => {
    fetchNews();
  }, [fetchNews]);

  const handleLoadMore = (): void => {
    const next = page + 1;
    const nextSlice = allItems.slice(0, (next + 1) * pageSize);
    setPage(next);
    setItems(nextSlice);
  };

  return (
    <>
      <div className="hero-section">
        <div className="hero-content">
          <h1>Inside Finance</h1>
          <p>Scopri le storie che muovono i mercati: analisi, trend e opportunità dal cuore della finanza globale.</p>
        </div>
        <div className="hero-overlay"></div>
      </div>

      <div className="home-container">
        {error && <div className="error-message">{error}</div>}

        <div className="news-section">
          <h2>
            {tagFilter ? `Notizie con tag: ${tagFilter}` : 'Tutte le Notizie'}
          </h2>
          {tagFilter && (
            <button 
              onClick={() => navigate('/home')} 
              className="btn-clear-filter"
            >
              ← Mostra tutte le notizie
            </button>
          )}
        </div>

        <div className="news-grid">
          {items.map((n) => (
            <article 
              key={String(n.id)} 
              className="news-card clickable-card"
              onClick={() => handleArticleClick(n.id)}
            >
              <div className="news-image-container">
                {n.imageUrl ? (
                  <img src={n.imageUrl} alt={n.title} className="news-image" />
                ) : (
                  <div className="news-image-placeholder">
                    <span>F</span>
                  </div>
                )}
                {n.tags && n.tags.length > 0 && (
                  <div className="news-tags-overlay">
                    {n.tags.map(tag => (
                      <span key={tag} className="tag">{tag}</span>
                    ))}
                  </div>
                )}
              </div>

              <div className="news-content">
                <div className="news-date">
                  {n.createdAt && new Date(n.createdAt).toLocaleDateString()}
                </div>
                <h3 className="news-title">{n.title}</h3>
                {n.summary && <p className="news-summary">{n.summary}</p>}
                <div className="read-more">Leggi di più →</div>
              </div>
            </article>
          ))}
        </div>

        {items.length === 0 && !loading && !error && (
          <div className="empty-state">
            <span className="empty-icon">📰</span>
            <h3>Nessuna notizia disponibile</h3>
            <p>Al momento non ci sono notizie da mostrare.</p>
          </div>
        )}

        {loading && (
          <div className="loading-state">
            <div className="loading-spinner"></div>
            <p>Caricamento notizie...</p>
          </div>
        )}

        {items.length > 0 && (
          <div className="home-actions">
            <button 
              onClick={handleLoadMore} 
              disabled={loading}
              className="load-more-button">
              {loading ? 'Caricamento...' : 'Carica altre notizie'}
            </button>
          </div>
        )}
      </div>
    </>
  );
};

export default Home;