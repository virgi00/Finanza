# 🚀 CI/CD Setup per Deploy Automatico su EC2

Questo repository è configurato per il deployment automatico su AWS EC2 tramite GitHub Actions.

## 📋 Setup Richiesto

### 1. Configurare GitHub Secrets

Nel tuo repository GitHub, vai su **Settings → Secrets and variables → Actions** e aggiungi:

| Secret Name | Valore | Descrizione |
|-------------|--------|-------------|
| `EC2_SSH_KEY` | Contenuto della chiave `finanza-key.pem` | Chiave privata per accesso SSH all'EC2 |
| `EC2_HOST` | `13.60.251.220` | IP pubblico dell'istanza EC2 |

### 2. Come ottenere EC2_SSH_KEY

```bash
cat finanza-key.pem
```

Copia **tutto** il contenuto (incluse le righe `-----BEGIN/END RSA PRIVATE KEY-----`) e incollalo come valore del secret.

## 🔄 Come funziona il CI/CD

### Trigger
- **Push** sul branch `main`
- **Pull Request** verso `main`

### Processo di Deploy
1. 📦 **Checkout** del codice
2. 🔑 **Setup** chiave SSH 
3. 📁 **Creazione** pacchetto deployment
4. 🚀 **Copia** files su EC2
5. 🐳 **Stop** container esistenti
6. 💾 **Backup** versione corrente
7. 🆕 **Deploy** nuova versione
8. 🔍 **Health check** automatico
9. ✅ **Conferma** o 🔄 **rollback** automatico

### Rollback Automatico
Se il health check fallisce, il sistema:
- Ferma la nuova versione
- Ripristina automaticamente la versione precedente
- Riavvia i container della versione funzionante

## 🌐 Accesso all'Applicazione

Dopo ogni deploy automatico:
- **Frontend**: http://13.60.251.220:3000
- **Backend**: http://13.60.251.220:8080

## 📝 Test del CI/CD

1. Fai una modifica al codice
2. Commit e push su `main`
3. Vai su **Actions** nel repository GitHub
4. Monitora il progresso del deployment
5. Verifica che l'applicazione sia aggiornata

## 🛠️ Troubleshooting

### Errori comuni:
- **SSH Permission denied**: Verifica che `EC2_SSH_KEY` contenga la chiave completa
- **Host verification failed**: Il secret `EC2_HOST` deve contenere solo l'IP
- **Docker build fails**: Controlla i log nell'action per errori di build

### Controllo manuale:
```bash
# SSH nell'EC2
ssh -i finanza-key.pem ubuntu@13.60.251.220

# Verifica container
cd /home/ubuntu/finanza-app
docker-compose -f docker-compose-ec2.yml ps
```