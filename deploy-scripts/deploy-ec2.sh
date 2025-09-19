#!/bin/bash

set -e

REGION="eu-north-1"
KEY_NAME="finanza-key"
INSTANCE_TYPE="t3.medium"
SECURITY_GROUP_NAME="finanza-sg"

echo "=== DEPLOYMENT SU EC2 ==="
echo "Regione: $REGION"
echo "Tipo istanza: $INSTANCE_TYPE"

# STEP 1: Creazione Key Pair
echo "=== STEP 1: Creazione Key Pair ==="
if ! aws ec2 describe-key-pairs --key-names $KEY_NAME --region $REGION >/dev/null 2>&1; then
    echo "Creazione key pair $KEY_NAME..."
    aws ec2 create-key-pair --key-name $KEY_NAME --region $REGION --query 'KeyMaterial' --output text > ${KEY_NAME}.pem
    chmod 400 ${KEY_NAME}.pem
    echo "Key pair salvata in ${KEY_NAME}.pem"
else
    echo "Key pair $KEY_NAME già esistente"
fi

# STEP 2: Creazione Security Group
echo "=== STEP 2: Creazione Security Group ==="
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=tag:Name,Values=finanza-vpc" --query 'Vpcs[0].VpcId' --output text --region $REGION)
if [ "$VPC_ID" = "None" ] || [ -z "$VPC_ID" ]; then
    VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query 'Vpcs[0].VpcId' --output text --region $REGION)
fi
echo "Using VPC: $VPC_ID"

# Controlla se il security group esiste già
SG_ID=$(aws ec2 describe-security-groups --filters "Name=group-name,Values=$SECURITY_GROUP_NAME" "Name=vpc-id,Values=$VPC_ID" --query 'SecurityGroups[0].GroupId' --output text --region $REGION 2>/dev/null || echo "None")

if [ "$SG_ID" = "None" ] || [ -z "$SG_ID" ]; then
    echo "Creazione security group $SECURITY_GROUP_NAME..."
    SG_ID=$(aws ec2 create-security-group \
        --group-name $SECURITY_GROUP_NAME \
        --description "Security group per Finanza app" \
        --vpc-id $VPC_ID \
        --region $REGION \
        --query 'GroupId' --output text)
    
    # Aggiungi regole
    aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 22 --cidr 0.0.0.0/0 --region $REGION  # SSH
    aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 80 --cidr 0.0.0.0/0 --region $REGION  # HTTP
    aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 443 --cidr 0.0.0.0/0 --region $REGION # HTTPS
    aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 8080 --cidr 0.0.0.0/0 --region $REGION # Backend
    aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 3306 --cidr 0.0.0.0/0 --region $REGION # MySQL
    
    echo "Security group creato: $SG_ID"
else
    echo "Security group già esistente: $SG_ID"
fi

# STEP 3: Creazione istanza EC2
echo "=== STEP 3: Creazione istanza EC2 ==="

# Ottieni l'AMI Ubuntu più recente
AMI_ID=$(aws ec2 describe-images \
    --owners 099720109477 \
    --filters "Name=name,Values=ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*" \
    --query 'Images | sort_by(@, &CreationDate) | [-1].ImageId' \
    --output text \
    --region $REGION)
echo "Using AMI: $AMI_ID"

# Ottieni una subnet pubblica
SUBNET_ID=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" "Name=map-public-ip-on-launch,Values=true" --query 'Subnets[0].SubnetId' --output text --region $REGION)
if [ "$SUBNET_ID" = "None" ] || [ -z "$SUBNET_ID" ]; then
    # Se non ci sono subnet pubbliche, usa la prima disponibile
    SUBNET_ID=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[0].SubnetId' --output text --region $REGION)
fi
echo "Using subnet: $SUBNET_ID"

# Script di inizializzazione
USER_DATA=$(cat << 'EOF'
#!/bin/bash
apt-get update
apt-get install -y docker.io docker-compose git

# Avvia Docker
systemctl start docker
systemctl enable docker
usermod -aG docker ubuntu

# Installa Docker Compose v2
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Crea directory per l'app
mkdir -p /home/ubuntu/finanza-app
chown ubuntu:ubuntu /home/ubuntu/finanza-app

# Installa Nginx
apt-get install -y nginx

echo "Setup completato" > /home/ubuntu/setup-complete.log
EOF
)

echo "Creazione istanza EC2..."
INSTANCE_ID=$(aws ec2 run-instances \
    --image-id $AMI_ID \
    --count 1 \
    --instance-type $INSTANCE_TYPE \
    --key-name $KEY_NAME \
    --security-group-ids $SG_ID \
    --subnet-id $SUBNET_ID \
    --associate-public-ip-address \
    --user-data "$USER_DATA" \
    --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=finanza-app-server}]" \
    --region $REGION \
    --query 'Instances[0].InstanceId' \
    --output text)

echo "Istanza EC2 creata: $INSTANCE_ID"

# STEP 4: Attendi che l'istanza sia in running
echo "=== STEP 4: Attesa avvio istanza ==="
echo "Attendo che l'istanza $INSTANCE_ID sia in stato 'running'..."
aws ec2 wait instance-running --instance-ids $INSTANCE_ID --region $REGION

# Ottieni l'IP pubblico
PUBLIC_IP=$(aws ec2 describe-instances \
    --instance-ids $INSTANCE_ID \
    --query 'Reservations[0].Instances[0].PublicIpAddress' \
    --output text \
    --region $REGION)

echo "=== DEPLOYMENT COMPLETATO ==="
echo "Istanza EC2 ID: $INSTANCE_ID"
echo "IP pubblico: $PUBLIC_IP"
echo "SSH: ssh -i ${KEY_NAME}.pem ubuntu@$PUBLIC_IP"
echo ""
echo "PROSSIMI PASSI:"
echo "1. Attendi 2-3 minuti per il completamento dell'inizializzazione"
echo "2. Connettiti via SSH per verificare che Docker sia installato"
echo "3. Carica il codice dell'applicazione"
echo "4. Esegui docker-compose up"
echo ""
echo "Per verificare l'inizializzazione:"
echo "ssh -i ${KEY_NAME}.pem ubuntu@$PUBLIC_IP 'cat /home/ubuntu/setup-complete.log'"
