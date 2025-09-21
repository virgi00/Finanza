#!/bin/bash

# Script per fare il rollback all'ultima versione funzionante
echo "⏪ Avvio rollback dell'applicazione Finanza..."

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BACKUP_DIR="/home/ubuntu/finanza-app-backup"
CURRENT_DIR="/home/ubuntu/finanza-app"

# Verifica se esiste un backup
if [ ! -d "$BACKUP_DIR" ]; then
    echo -e "${RED}❌ Nessun backup trovato in $BACKUP_DIR${NC}"
    echo "Impossibile effettuare il rollback."
    exit 1
fi

echo -e "${YELLOW}⚠️  Fermo i container attuali...${NC}"
cd "$CURRENT_DIR"
docker-compose -f docker-compose-ec2.yml down || true

echo -e "${YELLOW}📦 Ripristino versione precedente...${NC}"
cd /home/ubuntu
sudo rm -rf "$CURRENT_DIR" || true
sudo mv "$BACKUP_DIR" "$CURRENT_DIR" || {
    echo -e "${RED}❌ Errore durante il ripristino${NC}"
    exit 1
}

echo -e "${YELLOW}🚀 Riavvio applicazione...${NC}"
cd "$CURRENT_DIR"
docker-compose -f docker-compose-ec2.yml up -d

echo -e "${YELLOW}⏳ Attendo che i servizi si avviino...${NC}"
sleep 30

# Verifica che il rollback sia andato a buon fine
echo -e "${YELLOW}🔍 Verifico lo stato dopo rollback...${NC}"
if curl -f http://localhost:8080/articles > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Rollback completato con successo!${NC}"
    echo "Frontend: http://$(curl -s ifconfig.me):3000"
    echo "Backend: http://$(curl -s ifconfig.me):8080"
else
    echo -e "${RED}❌ Rollback fallito - i servizi non rispondono${NC}"
    exit 1
fi