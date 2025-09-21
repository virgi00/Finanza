# Deploy Scripts per Finanza App

Questa cartella contiene tutti gli script necessari per il deployment e la gestione dell'applicazione Finanza su AWS EC2.

## 📁 Script Disponibili

### 🚀 `deploy-ec2.sh`
Script principale per il deployment automatico dell'applicazione.
```bash
./deploy-ec2.sh
```

### ⚙️ `setup-ec2.sh`
Script per configurare l'ambiente EC2 la prima volta (installa Docker, Docker Compose, ecc.).
```bash
./setup-ec2.sh
```

### 🔍 `check-status.sh`
Script base per verificare lo stato dei container Docker.
```bash
./check-status.sh
```

### 🏥 `health-check.sh` (Nuovo)
Script avanzato per verificare lo stato di salute di tutti i servizi dell'applicazione.
```bash
./health-check.sh
```
**Caratteristiche:**
- Verifica stato Docker
- Test connettività backend e frontend
- Controllo connessione database
- Analisi logs per errori
- Output colorato e dettagliato

### ⏪ `rollback.sh` (Nuovo)
Script per effettuare il rollback all'ultima versione funzionante.
```bash
./rollback.sh
```
**Caratteristiche:**
- Ripristina automaticamente il backup precedente
- Riavvia i servizi
- Verifica che il rollback sia andato a buon fine

### 🔧 `manual-deploy.sh` (Nuovo)
Script per deployment manuale con controlli avanzati e rollback automatico in caso di fallimento.
```bash
./manual-deploy.sh
```
**Caratteristiche:**
- Verifica prerequisiti
- Backup automatico versione attuale
- Health check post-deployment
- Rollback automatico in caso di errore
- Cleanup automatico

## 🛠️ Utilizzo

### Prima configurazione su EC2:
```bash
# 1. Copia gli script sull'EC2
scp -i finanza-key.pem -r deploy-scripts/ ubuntu@YOUR_EC2_IP:/home/ubuntu/

# 2. Configura l'ambiente
ssh -i finanza-key.pem ubuntu@YOUR_EC2_IP
cd /home/ubuntu/deploy-scripts
./setup-ec2.sh
```

### Deployment manuale:
```bash
# 1. Crea il pacchetto di deployment
tar -czf finanza-app-deploy.tar.gz backend/ frontend/ mysql/ docker-compose-ec2.yml deploy-scripts/

# 2. Copia sull'EC2
scp -i finanza-key.pem finanza-app-deploy.tar.gz ubuntu@YOUR_EC2_IP:/home/ubuntu/

# 3. Esegui il deployment
ssh -i finanza-key.pem ubuntu@YOUR_EC2_IP
cd /home/ubuntu/deploy-scripts
./manual-deploy.sh
```

### Verifica stato applicazione:
```bash
ssh -i finanza-key.pem ubuntu@YOUR_EC2_IP
cd /home/ubuntu/deploy-scripts
./health-check.sh
```

### Rollback in caso di problemi:
```bash
ssh -i finanza-key.pem ubuntu@YOUR_EC2_IP
cd /home/ubuntu/deploy-scripts
./rollback.sh
```

## 🔄 Deployment Automatico (GitHub Actions)

Il deployment automatico è configurato tramite GitHub Actions. Ad ogni push su `main`:

1. Il workflow crea automaticamente il pacchetto
2. Lo copia sull'EC2
3. Esegue il deployment con controlli automatici
4. Effettua rollback automatico in caso di errore

### Configurazione GitHub Secrets:
- `EC2_SSH_KEY`: Chiave privata SSH (contenuto di finanza-key.pem)
- `EC2_HOST`: IP Elastico dell'istanza EC2 (`13.53.121.229`)

## 📋 Note Importanti

- Tutti gli script includono controlli di errore e output colorato
- Il backup automatico protegge da deployment falliti
- Gli script sono progettati per essere idempotenti (sicuri da rieseguire)
- Assicurarsi sempre che l'Elastic IP sia configurato per evitare problemi con CI/CD

## 🆘 Troubleshooting

Se qualcosa va storto:

1. **Controlla lo stato**: `./health-check.sh`
2. **Verifica i logs**: `docker-compose -f docker-compose-ec2.yml logs`
3. **Rollback**: `./rollback.sh`
4. **Riparti da zero**: `./setup-ec2.sh` seguito da `./manual-deploy.sh`