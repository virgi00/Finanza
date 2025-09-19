#!/bin/bash

# Script da eseguire sull'istanza EC2 per il setup dell'applicazione

set -e

echo "=== SETUP APPLICAZIONE FINANZA SU EC2 ==="

# Verifica che Docker sia installato
if ! command -v docker &> /dev/null; then
    echo "ERRORE: Docker non è installato!"
    exit 1
fi

# Verifica che Docker Compose sia installato
if ! command -v docker-compose &> /dev/null; then
    echo "ERRORE: Docker Compose non è installato!"
    exit 1
fi

# Crea la directory dell'app se non esiste
mkdir -p /home/ubuntu/finanza-app
cd /home/ubuntu/finanza-app

echo "Directory corrente: $(pwd)"

echo "=== STEP 1: Verifica struttura progetto ==="
if [ ! -f "docker-compose-ec2.yml" ]; then
    echo "ERRORE: File docker-compose-ec2.yml non trovato!"
    echo "Assicurati di aver caricato tutti i file del progetto in /home/ubuntu/finanza-app/"
    exit 1
fi

if [ ! -d "backend" ]; then
    echo "ERRORE: Directory backend non trovata!"
    exit 1
fi

if [ ! -d "frontend" ]; then
    echo "ERRORE: Directory frontend non trovata!"
    exit 1
fi

if [ ! -d "mysql" ]; then
    echo "ERRORE: Directory mysql non trovata!"
    exit 1
fi

echo "✅ Struttura progetto verificata"

echo "=== STEP 2: Arresto eventuali container esistenti ==="
docker-compose -f docker-compose-ec2.yml down -v || true

echo "=== STEP 3: Pulizia immagini vecchie ==="
docker system prune -f

echo "=== STEP 4: Build delle immagini ==="
echo "Building MySQL image..."
docker build -t finanza-mysql -f - . << 'EOF'
FROM mysql:8.0
COPY mysql/init.sql /docker-entrypoint-initdb.d/
EOF

echo "Building backend image..."
docker-compose -f docker-compose-ec2.yml build backend

echo "Building frontend image..."
docker-compose -f docker-compose-ec2.yml build frontend

echo "=== STEP 5: Avvio dei servizi ==="
docker-compose -f docker-compose-ec2.yml up -d

echo "=== STEP 6: Verifica dei servizi ==="
sleep 30

echo "Stato dei container:"
docker-compose -f docker-compose-ec2.yml ps

echo "Log MySQL:"
docker-compose -f docker-compose-ec2.yml logs mysql | tail -10

echo "Log Backend:"
docker-compose -f docker-compose-ec2.yml logs backend | tail -10

echo "Log Frontend:"
docker-compose -f docker-compose-ec2.yml logs frontend | tail -10

echo "=== DEPLOYMENT COMPLETATO ==="
echo "L'applicazione dovrebbe essere disponibile su:"
PUBLIC_IP=$(curl -s http://checkip.amazonaws.com/)
echo "http://$PUBLIC_IP"
echo ""
echo "Per monitorare i log:"
echo "docker-compose -f docker-compose-ec2.yml logs -f"
echo ""
echo "Per verificare lo stato:"
echo "docker-compose -f docker-compose-ec2.yml ps"
