// API Configuration
const getApiBaseUrl = (): string => {
  // Se REACT_APP_API_URL è definita, usa quella
  if (process.env.REACT_APP_API_URL) {
    return process.env.REACT_APP_API_URL;
  }
  
  // Altrimenti, rileva automaticamente l'ambiente
  if (process.env.NODE_ENV === 'production') {
    // In produzione, usa l'host corrente sulla porta 8080
    return `${window.location.protocol}//${window.location.hostname}:8080`;
  }
  
  // In sviluppo, usa localhost
  return 'http://localhost:8080';
};

export const API_BASE_URL = getApiBaseUrl();

export default {
  BASE_URL: API_BASE_URL
};
