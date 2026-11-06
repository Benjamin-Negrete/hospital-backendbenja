#!/bin/bash
echo "=== Uniendo rabbitmq2 al clúster de rabbitmq1 ==="
docker exec -it rabbitmq2 rabbitmqctl stop_app
docker exec -it rabbitmq2 rabbitmqctl reset
docker exec -it rabbitmq2 rabbitmqctl join_cluster rabbit@rabbitmq1
docker exec -it rabbitmq2 rabbitmqctl start_app
echo "=== Estado del clúster ==="
docker exec -it rabbitmq1 rabbitmqctl cluster_status
echo "=== Clúster configurado exitosamente con 2 nodos! ==="
