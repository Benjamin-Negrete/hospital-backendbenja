@echo off
echo === Uniendo rabbitmq2 al cluster de rabbitmq1 ===
docker exec -it rabbitmq2 rabbitmqctl stop_app
docker exec -it rabbitmq2 rabbitmqctl reset
docker exec -it rabbitmq2 rabbitmqctl join_cluster rabbit@rabbitmq1
docker exec -it rabbitmq2 rabbitmqctl start_app
echo === Estado del cluster ===
docker exec -it rabbitmq1 rabbitmqctl cluster_status
echo === Cluster configurado exitosamente con 2 nodos! ===
pause
