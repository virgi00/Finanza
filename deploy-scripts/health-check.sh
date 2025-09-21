#!/bin/bash

# Script per verificare lo stato di salute dell'applicazione
echo "🔍 Verifica stato applicazione Finanza..."

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Funzione per verificare un servizio
check_service() {
    local service_name=$1
    local url=$2
    local expected_status=${3:-200}
    
    echo -n "📋 Controllo $service_name... "
    
    response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
    
    if [ "$response" = "$expected_status" ]; then
        echo -e "${GREEN}✅ OK (HTTP $response)${NC}"
        return 0
    else
        echo -e "${RED}❌ FAIL (HTTP $response)${NC}"
        return 1
    fi
}

# Controlla se Docker è in esecuzione
echo "🐳 Verifica Docker..."
if ! docker ps > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker non è in esecuzione${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker è attivo${NC}"

# Controlla i container
echo ""
echo "📦 Stato dei container:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "🌐 Test connettività servizi:"

# Test dei servizi
BACKEND_URL="http://localhost:8080"
FRONTEND_URL="http://localhost:3000"

# Test backend
check_service "Backend API" "$BACKEND_URL/articles"
check_service "Backend Health" "$BACKEND_URL/actuator/health" || check_service "Backend Health" "$BACKEND_URL/health"

# Test frontend
check_service "Frontend" "$FRONTEND_URL"

# Test database connection tramite backend
echo -n "🗄️  Controllo connessione database... "
if curl -s "$BACKEND_URL/articles" | grep -q "\[\]"; then
    echo -e "${GREEN}✅ Database connesso${NC}"
else
    echo -e "${YELLOW}⚠️  Database potrebbe non essere inizializzato o contenere dati${NC}"
fi

# Verifica logs per errori
echo ""
echo "📋 Ultimi logs (errori):"
docker-compose -f docker-compose-ec2.yml logs --tail=5 | grep -i error || echo "Nessun errore trovato negli ultimi logs"

echo ""
echo "🎯 Verifica completata!"
echo "Frontend: http://$(curl -s ifconfig.me):3000"
echo "Backend: http://$(curl -s ifconfig.me):8080"