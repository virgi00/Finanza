#!/bin/bash

# Script per aggiornare manualmente l'applicazione sull'EC2
echo "🚀 Aggiornamento manuale dell'applicazione Finanza..."

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variabili
DEPLOY_FILE="finanza-app-deploy.tar.gz"
APP_DIR="/home/ubuntu/finanza-app"
BACKUP_DIR="/home/ubuntu/finanza-app-backup"

# Funzione per verificare se un comando è disponibile
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}❌ $1 non è installato${NC}"
        exit 1
    fi
}

# Verifica prerequisiti
echo -e "${BLUE}🔍 Verifica prerequisiti...${NC}"
check_command docker
check_command docker-compose

# Verifica se il file di deploy esiste
if [ ! -f "$DEPLOY_FILE" ]; then
    echo -e "${RED}❌ File $DEPLOY_FILE non trovato${NC}"
    echo "Assicurati di aver creato il pacchetto di deployment con:"
    echo "tar -czf $DEPLOY_FILE backend/ frontend/ mysql/ docker-compose-ec2.yml deploy-scripts/"
    exit 1
fi

echo -e "${YELLOW}⏳ Fermo i container attuali...${NC}"
if [ -d "$APP_DIR" ]; then
    cd "$APP_DIR"
    docker-compose -f docker-compose-ec2.yml down || true
    
    # Backup della versione attuale
    echo -e "${YELLOW}💾 Backup versione attuale...${NC}"
    sudo rm -rf "$BACKUP_DIR" || true
    sudo mv "$APP_DIR" "$BACKUP_DIR" || true
fi

echo -e "${YELLOW}📦 Estrazione nuova versione...${NC}"
cd /home/ubuntu
tar -xzf "$DEPLOY_FILE"
mkdir finanza-app-new
mv backend frontend mysql docker-compose-ec2.yml deploy-scripts finanza-app-new/
mv finanza-app-new "$APP_DIR"

echo -e "${YELLOW}🔧 Rendere eseguibili gli script...${NC}"
chmod +x "$APP_DIR/deploy-scripts/"*.sh

echo -e "${YELLOW}🚀 Avvio nuovi container...${NC}"
cd "$APP_DIR"
docker-compose -f docker-compose-ec2.yml up -d --build

echo -e "${YELLOW}⏳ Attendo che i servizi si avviino...${NC}"
sleep 30

# Health check
echo -e "${BLUE}🔍 Verifica stato servizi...${NC}"
if curl -f http://localhost:8080/articles > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Aggiornamento completato con successo!${NC}"
    echo "Frontend: http://$(curl -s ifconfig.me):3000"
    echo "Backend: http://$(curl -s ifconfig.me):8080"
    
    # Cleanup backup se tutto è OK
    echo -e "${YELLOW}🧹 Pulizia backup...${NC}"
    sudo rm -rf "$BACKUP_DIR" || true
else
    echo -e "${RED}❌ Aggiornamento fallito - Avvio rollback automatico${NC}"
    cd /home/ubuntu
    docker-compose -f "$APP_DIR/docker-compose-ec2.yml" down || true
    sudo rm -rf "$APP_DIR" || true
    
    if [ -d "$BACKUP_DIR" ]; then
        sudo mv "$BACKUP_DIR" "$APP_DIR" || true
        cd "$APP_DIR"
        docker-compose -f docker-compose-ec2.yml up -d
        echo -e "${YELLOW}⏪ Rollback completato${NC}"
    fi
    exit 1
fi

# Cleanup file di deploy
rm -f "/home/ubuntu/$DEPLOY_FILE"
echo -e "${GREEN}🎉 Deploy completato!${NC}"