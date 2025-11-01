# AYGO - Sistema Distribuido

Este proyecto implementa un sistema distribuido con:
- 3 instancias backend Spring Boot conectadas a MongoDB
- Un balanceador de carga Nginx
- Frontend simple con JavaScript y HTML

## Estructura
- **DistributedApp/** → código del backend
- **LoadBalancer/** → configuración Nginx y frontend
- **docker-compose.yml** → despliega todo el sistema

## Cómo ejecutar
```bash
docker-compose up --build
