// src/pages/ArticlePage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { API_BASE_URL } from '../config/api';

interface Article {
  id: string | number;
  title: string;
  body: string;
  date?: string;
  srcImg?: string;
  tags?: Array<string | { name?: string; tagName?: string }>;
}

const ArticlePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [article, setArticle] = useState<Article | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const handleTagClick = (tag: string) => {
    console.log('[ArticlePage] Tag clicked:', tag);
    navigate(`/home?tag=${encodeURIComponent(tag)}`);
  };

  useEffect(() => {
    const fetchArticle = async () => {
      if (!id) {
        setError('ID articolo non valido');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);

        const response = await fetch(`${API_BASE_URL}/articles/searchById/${id}`);
        
        if (!response.ok) {
          if (response.status === 404) {
            setError('Articolo non trovato');
          } else {
            const errorText = await response.text().catch(() => '');
            setError(errorText || `Errore HTTP ${response.status}`);
          }
          return;
        }

        const data = await response.json();
        setArticle(data);
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Errore sconosciuto';
        console.error('[ArticlePage] Error fetching article:', err);
        setError(message);
      } finally {
        setLoading(false);
      }
    };

    fetchArticle();
  }, [id]);

  // Clean article body from metadata
  const cleanArticleBody = (body: string): string => {
    let cleanBody = body;
    cleanBody = cleanBody.replace(/Tags:.*$/gm, ''); // Remove Tags: lines
    cleanBody = cleanBody.replace(/Category:.*$/gm, ''); // Remove Category: lines
    cleanBody = cleanBody.replace(/#\w+/g, ''); // Remove hashtags
    cleanBody = cleanBody.replace(/\n\s*\n/g, '\n'); // Remove multiple empty lines
    return cleanBody.trim();
  };

  // Extract tags from article
  const getTags = (tags?: Array<string | { name?: string; tagName?: string }>): string[] => {
    if (!tags) return [];
    return tags.map(tag => {
      if (typeof tag === 'string') return tag;
      return tag.name || (tag as any).tagName || '';
    }).filter(Boolean);
  };

  if (loading) {
    return (
      <>
        <nav className="top-nav">
          <Link to="/" className="brand">Finanza</Link>
          <div className="nav-right">
            <Link to="/home" className="nav-btn">Home</Link>
          </div>
        </nav>
        <div style={{ height: '60px' }} />
        <div className="article-container">
          <div className="loading-state">
            <div className="loading-spinner"></div>
            <p>Caricamento articolo...</p>
          </div>
        </div>
      </>
    );
  }

  if (error) {
    return (
      <>
        <nav className="top-nav">
          <Link to="/" className="brand">Finanza</Link>
          <div className="nav-right">
            <Link to="/home" className="nav-btn">Home</Link>
          </div>
        </nav>
        <div style={{ height: '60px' }} />
        <div className="article-container">
          <div className="error-message">{error}</div>
          <button onClick={() => navigate('/home')} className="btn-secondary">
            Torna alla Home
          </button>
        </div>
      </>
    );
  }

  if (!article) {
    return (
      <>
        <nav className="top-nav">
          <Link to="/" className="brand">Finanza</Link>
          <div className="nav-right">
            <Link to="/home" className="nav-btn">Home</Link>
          </div>
        </nav>
        <div style={{ height: '60px' }} />
        <div className="article-container">
          <div className="empty-state">
            <h3>Articolo non trovato</h3>
            <p>L'articolo richiesto non esiste o è stato rimosso.</p>
            <button onClick={() => navigate('/home')} className="btn-secondary">
              Torna alla Home
            </button>
          </div>
        </div>
      </>
    );
  }

  const tags = getTags(article.tags);
  const cleanBody = cleanArticleBody(article.body);

  return (
    <>
      <nav className="top-nav">
        <Link to="/" className="brand">Finanza</Link>
        <div className="nav-right">
          <Link to="/home" className="nav-btn">Home</Link>
        </div>
      </nav>
      <div style={{ height: '60px' }} />
      
      <div className="article-container">
        <button onClick={() => navigate(-1)} className="btn-back">
          ← Indietro
        </button>
        
        <article className="article-content">
          <header className="article-header">
            <h1 className="article-title">{article.title}</h1>
            
            {article.date && (
              <div className="article-meta">
                <time className="article-date">
                  {new Date(article.date).toLocaleDateString('it-IT', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </time>
              </div>
            )}

            {tags.length > 0 && (
              <div className="article-tags">
                {tags.map(tag => (
                  <button
                    key={tag} 
                    type="button"
                    className="tag clickable-tag"
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      handleTagClick(tag);
                    }}
                    title={`Visualizza altre notizie con tag: ${tag}`}
                  >
                    {tag}
                  </button>
                ))}
              </div>
            )}
          </header>

          {article.srcImg && (
            <div className="article-image-container">
              <img 
                src={article.srcImg} 
                alt={article.title} 
                className="article-image"
                onError={(e) => {
                  (e.target as HTMLImageElement).style.display = 'none';
                }}
              />
            </div>
          )}

          <div className="article-body">
            {cleanBody.split('\n').map((paragraph, index) => (
              paragraph.trim() && <p key={index}>{paragraph}</p>
            ))}
          </div>
        </article>
      </div>
    </>
  );
};

export default ArticlePage;
