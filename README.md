# Finanza App - Guida Docker

Questa applicazione è composta da un backend Java Spring Boot e un frontend React, containerizzati con Docker.

## Prerequisiti

- Docker
- Docker Compose

## Struttura del Progetto

```
.
├── backend/
│   ├── Dockerfile
│   ├── .dockerignore
│   └── src/
├── frontend/
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── nginx.conf
│   └── src/
├── mysql/
│   └── init.sql
├── docker-compose.yml
├── .env
└── README-Docker.md
```

## Configurazione

1. **Modifica il file `.env`** con le tue credenziali:
   - Cambia le password del database
   - Inserisci la tua vera API key di Gemini
   - Personalizza altre variabili se necessario

2. **Verifica le configurazioni** nei file:
   - `backend/src/main/resources/application-docker.properties`
   - `frontend/nginx.conf`

## Avvio dell'Applicazione

### Avvio Completo
```bash
# Avvia tutti i servizi
docker-compose up -d

# Visualizza i log
docker-compose logs -f
```

### Avvio Singoli Servizi
```bash
# Solo il database
docker-compose up -d mysql

# Backend e database
docker-compose up -d mysql backend

# Tutti i servizi
docker-compose up -d
```

## Accesso ai Servizi

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **Database MySQL**: localhost:3307

## Comandi Utili

```bash
# Ferma tutti i servizi
docker-compose down

# Ferma e rimuove i volumi (ATTENZIONE: cancella i dati del database)
docker-compose down -v

# Ricostruisci e riavvia
docker-compose up --build -d

# Visualizza lo stato dei container
docker-compose ps

# Accedi al container del backend
docker-compose exec backend bash

# Accedi al database MySQL
docker-compose exec mysql mysql -u root -p finanzanewsdb
```

## Risoluzione Problemi

### Il backend non si connette al database
- Verifica che MySQL sia avviato: `docker-compose logs mysql`
- Controlla le credenziali nel file `.env`

### Errori di build
- Pulisci i container: `docker-compose down`
- Ricostruisci: `docker-compose build --no-cache`

### Porte già in uso
- Cambia le porte nel `docker-compose.yml` se sono già occupate

## Sviluppo

Per lo sviluppo locale, puoi usare:
```bash
# Avvia solo il database
docker-compose up -d mysql

# Poi avvia backend e frontend localmente
```

## Produzione

Per la produzione:
1. Modifica tutte le password e chiavi API nel file `.env`
2. Considera l'uso di Docker Secrets per informazioni sensibili
3. Configura un reverse proxy (nginx/traefik) se necessario
4. Implementa backup del database
