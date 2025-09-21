#!/bin/bash

# Script per monitorare lo stato del deployment

REGION="eu-north-1"
CLUSTER_NAME="finanza-cluster"
SERVICE_NAME="finanza-service"

echo "=== STATUS DEL CLUSTER ==="
aws ecs describe-clusters --clusters $CLUSTER_NAME --region $REGION

echo "=== STATUS DEL SERVIZIO ==="
aws ecs describe-services --cluster $CLUSTER_NAME --services $SERVICE_NAME --region $REGION

echo "=== TASK IN ESECUZIONE ==="
TASK_ARNS=$(aws ecs list-tasks --cluster $CLUSTER_NAME --service-name $SERVICE_NAME --region $REGION --query 'taskArns' --output text)

if [ ! -z "$TASK_ARNS" ]; then
  echo "Task ARNs: $TASK_ARNS"
  aws ecs describe-tasks --cluster $CLUSTER_NAME --tasks $TASK_ARNS --region $REGION
  
  echo "=== LOG DEI CONTAINER ==="
  for TASK_ARN in $TASK_ARNS; do
    echo "Task: $TASK_ARN"
    # Ottieni i log degli ultimi 10 minuti
    aws logs filter-log-events --log-group-name "/ecs/finanza-app" --start-time $(date -d '10 minutes ago' +%s)000 --region $REGION
  done
else
  echo "Nessun task in esecuzione"
fi

echo "=== STATUS LOAD BALANCER ==="
aws elbv2 describe-load-balancers --names finanza-alb --region $REGION 2>/dev/null || echo "Load Balancer non trovato"

echo "=== TARGET GROUPS ==="
aws elbv2 describe-target-groups --names finanza-tg --region $REGION 2>/dev/null || echo "Target Group non trovato"
