# Progetto Finanza News

## Indice

1.  [Panoramica del Progetto](#panoramica-del-progetto)
2.  [Architettura del Sistema](#architettura-del-sistema)
    -   [Backend](#backend)
    -   [Frontend](#frontend)
    -   [Database](#database)
3.  [Stack Tecnologico](#stack-tecnologico)
4.  [Funzionalità Principali](#funzionalità-principali)
5.  [Prerequisiti di Sistema](#prerequisiti-di-sistema)
6.  [Configurazione dell'Ambiente](#configurazione-dellambiente)
    -   [File di Ambiente (`.env`)](#file-di-ambiente-env)
    -   [Configurazioni Aggiuntive](#configurazioni-aggiuntive)
7.  [Esecuzione in Ambiente di Sviluppo Locale](#esecuzione-in-ambiente-di-sviluppo-locale)
    -   [Avvio Completo](#avvio-completo)
    -   [Gestione dei Servizi](#gestione-dei-servizi)
8.  [Deployment su AWS EC2](#deployment-su-aws-ec2)
    -   [CI/CD con GitHub Actions](#cicd-con-github-actions)
    -   [Script di Deployment](#script-di-deployment)
9.  [Endpoint API Principali](#endpoint-api-principali)
10. [Gestione e Manutenzione](#gestione-e-manutenzione)

---

## Panoramica del Progetto

**Finanza News** è una piattaforma web full-stack progettata per la generazione e consultazione di notizie finanziarie. Il sistema utilizza l'intelligenza artificiale per creare articoli di notizie basati su input testuali, che vengono poi archiviati, categorizzati e resi disponibili agli utenti tramite un'interfaccia web reattiva. L'applicazione è completamente containerizzata con Docker per garantire portabilità e coerenza tra gli ambienti di sviluppo e produzione.

## Architettura del Sistema

Il progetto adotta un'architettura a microservizi, composta da tre componenti principali che operano in modo indipendente ma coordinato.

### Backend

Servizio basato su **Java Spring Boot** che espone API RESTful per la gestione delle funzionalità core. Le sue responsabilità includono:
- Autenticazione e autorizzazione degli utenti (basata su token JWT).
- Interazione con l'API di Google Gemini per la generazione di contenuti testuali.
- CRUD (Create, Read, Update, Delete) per articoli, categorie e utenti.
- Logica di business per l'elaborazione e l'archiviazione dei dati.

### Frontend

Applicazione Single-Page Application (SPA) sviluppata in **React** e servita tramite un server **Nginx**. L'interfaccia utente consente di:
- Registrare e autenticare gli utenti.
- Visualizzare un elenco di articoli con funzionalità di ricerca e filtro.
- Leggere il contenuto completo di un articolo.
- Accedere a una dashboard per la generazione di nuovi articoli tramite l'invio di prompt testuali.

### Database

Un'istanza **MySQL** viene utilizzata come database relazionale per la persistenza dei dati. Memorizza informazioni relative a:
- Utenti e relative credenziali.
- Articoli generati, inclusi titolo, corpo, data di pubblicazione.
- Categorie e tag associati agli articoli.

## Stack Tecnologico

- **Backend**: Java 17, Spring Boot 3, Spring Security, JPA (Hibernate), JWT, Maven.
- **Frontend**: React 18, TypeScript, Axios, React Router.
- **Database**: MySQL 8.0.
- **Generazione Contenuti**: Google Gemini API.
- **Containerizzazione**: Docker, Docker Compose.
- **Web Server (Frontend)**: Nginx.
- **CI/CD**: GitHub Actions.
- **Cloud Provider**: AWS (EC2 per l'hosting).

## Funzionalità Principali

- **Autenticazione Utente**: Sistema di registrazione e login sicuro con JWT.
- **Generazione Articoli via AI**: Creazione di articoli finanziari a partire da un prompt, utilizzando Google Gemini.
- **Dashboard di Gestione**: Interfaccia per la visualizzazione e la ricerca di articoli.
- **Categorizzazione Automatica**: Estrazione e assegnazione automatica di titolo, corpo, tag e categoria dal testo generato.
- **Deployment Automatizzato**: Pipeline CI/CD che distribuisce automaticamente le modifiche del branch `main` sull'istanza EC2.

## Prerequisiti di Sistema

- Docker Engine
- Docker Compose

## Configurazione dell'Ambiente

Prima di avviare l'applicazione, è necessario configurare le variabili d'ambiente.

### File di Ambiente (`.env`)

Creare un file `.env` nella directory principale del progetto e configurare le seguenti variabili:

```env
# Credenziali Database MySQL
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_DATABASE=finanzanewsdb
MYSQL_USER=your_user
MYSQL_PASSWORD=your_password

# API Key per il servizio di generazione contenuti
GEMINI_API_KEY=your_gemini_api_key

# Segreto per la firma dei token JWT
JWT_SECRET=your_jwt_secret
```

### Configurazioni Aggiuntive

- **Backend**: Il file `backend/src/main/resources/application-docker.properties` è configurato per leggere le variabili d'ambiente e connettersi ai servizi Docker.
- **Frontend**: Il file `frontend/nginx.conf` gestisce il reverse proxy per le chiamate API al backend.

## Esecuzione in Ambiente di Sviluppo Locale

L'intera applicazione può essere avviata tramite Docker Compose.

### Avvio Completo

Eseguire il seguente comando dalla directory principale per avviare tutti i servizi in background:
```bash
docker-compose up -d
```

### Gestione dei Servizi

- **Visualizzare i log**: `docker-compose logs -f`
- **Fermare i servizi**: `docker-compose down`
- **Fermare e rimuovere i volumi** (ATTENZIONE: i dati del database verranno persi): `docker-compose down -v`
- **Ricostruire le immagini**: `docker-compose up --build -d`
- **Verificare lo stato dei container**: `docker-compose ps`

I servizi saranno accessibili ai seguenti indirizzi:
- **Frontend**: `http://localhost:3000`
- **Backend API**: `http://localhost:8080`
- **Database (porta esposta)**: `localhost:3307`

## Deployment su AWS EC2

### CI/CD con GitHub Actions

Il repository è configurato con una pipeline di GitHub Actions (`.github/workflows/deploy.yml`) che si attiva ad ogni push sul branch `main`. La pipeline esegue i seguenti passaggi:
1.  Effettua il checkout del codice.
2.  Configura una chiave SSH per l'accesso all'istanza EC2.
3.  Crea un archivio `.tar.gz` contenente i file necessari per il deployment.
4.  Copia l'archivio sull'istanza EC2.
5.  Si connette via SSH all'istanza ed esegue lo script di deployment, che si occupa di:
    -   Fermare i container esistenti.
    -   Creare un backup della versione corrente.
    -   Estrarre la nuova versione.
    -   Avviare i nuovi container.
    -   Eseguire un health check e un eventuale rollback in caso di fallimento.

Per il corretto funzionamento della pipeline, è necessario configurare i seguenti **secrets** nel repository GitHub:
- `EC2_HOST`: L'indirizzo IP (preferibilmente un Elastic IP) dell'istanza EC2.
- `EC2_SSH_KEY`: La chiave SSH privata per l'accesso all'istanza.

### Script di Deployment

La cartella `deploy-scripts/` contiene script ausiliari per la gestione dell'ambiente in produzione, tra cui:
- `setup-ec2.sh`: Per la configurazione iniziale dell'istanza.
- `health-check.sh`: Per verificare lo stato di salute dei servizi.
- `rollback.sh`: Per ripristinare manualmente la versione precedente.

## Endpoint API Principali

Il backend espone diverse API, tra cui:

- `POST /auth/register`: Registrazione di un nuovo utente.
- `POST /auth/login`: Autenticazione e rilascio di un token JWT.
- `POST /gemini/summarize`: Generazione di un nuovo articolo (richiede autenticazione).
- `GET /articles`: Recupero di tutti gli articoli.
- `GET /articles/{id}`: Recupero di un singolo articolo.

## Gestione e Manutenzione

- **Accesso ai container**: `docker-compose exec <service_name> bash`
- **Accesso al database**: `docker-compose exec mysql mysql -u root -p finanzanewsdb`
- **Troubleshooting**: Analizzare i log dei container per identificare la causa di eventuali problemi. Verificare che le variabili d'ambiente e le configurazioni di rete tra i container siano corrette.
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
# Updated Sun Sep 21 20:02:46 CEST 2025
Deploy: Update EC2_HOST IP to match current instance
Deploy: Force redeploy to ensure frontend update on EC2
Force deploy: Ensure new Gemini API key is active on EC2
