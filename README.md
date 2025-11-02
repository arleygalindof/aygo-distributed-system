# AYGO — Sistema Distribuido con Balanceador de Carga, Backends y MongoDB usando Robin Round Service

## 📘 Descripción General

**Aygo** es una aplicación distribuida diseñada para demostrar una arquitectura **balanceada y escalable** basada en contenedores **Docker**.  
El sistema cuenta con un **balanceador Nginx**, tres **instancias backend Java (Spring Boot)** y una base de datos **MongoDB**.  
El despliegue completo se realiza mediante **Docker Compose** sobre una instancia **AWS EC2**.

---

## 🏗️ Arquitectura del Proyecto

La arquitectura de Aygo está organizada en los siguientes componentes:
             ┌──────────────────────────┐
             │      Cliente / Browser   │
             └─────────────┬────────────┘
                           │  HTTP (8087)
                    ┌──────▼──────┐
                    │ LoadBalancer│
                    │ (Nginx)     │
                    └──────┬──────┘
           ┌───────────────┼────────────────┐
           │               │                │
   ┌───────▼──────┐ ┌──────▼──────┐ ┌───────▼──────┐
   │ Backend #1    │ │ Backend #2   │ │ Backend #3 │
   │ Spring Boot   │ │ Spring Boot  │ │ Spring Boot│
   └──────┬────────┘ └──────┬──────┘ └───────┬─────┘
          │                 │                │
          └─────────────────┴────────────────┘
                            │
                     ┌──────▼──────┐
                     │   MongoDB   │
                     └─────────────┘


### Componentes
- **aygo-loadbalancer** → Servidor Balanceador tipo Nginx que distribuye las peticiones HTTP entre los backends.  
- **aygo-backend1, 2 y 3* → Aplicaciones Java (Spring Boot) que procesan las solicitudes REST.  
- **aygo-mongo** → Base de datos MongoDB utilizada por los backends.  

Todos los servicios se comunican dentro de una red interna llamada `aygo-network`.

---

## ⚙️ Estructura del Repositorio

├── aygo-backend1/
│ ├── Dockerfile
│ ├── src/
│ └── target/
│
├── aygo-backend2/
│ ├── Dockerfile
│ ├── src/
│ └── target/
│
├── aygo-backend3/
│ ├── Dockerfile
│ ├── src/
│ └── target/
│
├── LoadBalancer/
│ ├── nginx.conf
│ ├── Dockerfile
│ └── src/main/resources/static/
│ └── index.html
│
├── docker-compose.yml
└── README.md


---

## 🚀 Despliegue en AWS EC2

### 1️⃣ Conectarse a la instancia

Imágenes Docker

<img width="1586" height="171" alt="image" src="https://github.com/user-attachments/assets/3690b54a-2084-4e8e-b316-0e7a1241f9de" />

Levantar la aplicación

docker compose up -d --build

Acceso a la Aplicación

Frontend / Balanceador:
👉 http://ec2-XX-XX-XX-XX.compute-1.amazonaws.com:8087/

Endpoints Backend (directos):

Backend 1 → http://ec2-XX-XX-XX-XX.compute-1.amazonaws.com:8081/api/...

Backend 2 → http://ec2-XX-XX-XX-XX.compute-1.amazonaws.com:8082/api/...

Backend 3 → http://ec2-XX-XX-XX-XX.compute-1.amazonaws.com:8083/api/...

🔁 El balanceador distribuye automáticamente las peticiones entre los tres backends mediante round-robin.

Contenido del docker-compose.yml

---
services:
  aygo-mongo:
    image: arleygf/aygo-mongo:3.6.1
    container_name: aygo-mongo
    ports:
      - "27018:27017"
    networks:
      - aygo-network

  aygo-backend1:
    image: arleygf/aygo-backend1:latest
    container_name: aygo-backend1
    depends_on:
      - aygo-mongo
    ports:
      - "8081:8080"
    networks:
      - aygo-network

  aygo-backend2:
    image: arleygf/aygo-backend2:latest
    container_name: aygo-backend2
    depends_on:
      - aygo-mongo
    ports:
      - "8082:8080"
    networks:
      - aygo-network

  aygo-backend3:
    image: arleygf/aygo-backend3:latest
    container_name: aygo-backend3
    depends_on:
      - aygo-mongo
    ports:
      - "8083:8080"
    networks:
      - aygo-network

  aygo-loadbalancer:
    build: ./LoadBalancer
    image: arleygf/aygo-loadbalancer:latest
    container_name: aygo-loadbalancer
    ports:
      - "8087:80"
    volumes:
      - ./LoadBalancer/src/main/resources/static:/usr/share/nginx/html:ro
      - ./LoadBalancer/nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - aygo-backend1
      - aygo-backend2
      - aygo-backend3
    networks:
      - aygo-network

networks:
  aygo-network:
    driver: bridge
---

🧩 Configuración de Nginx

Archivo: LoadBalancer/nginx.conf

---
events {}

http {
    upstream backend_cluster {
        server aygo-backend1:8080;
        server aygo-backend2:8080;
        server aygo-backend3:8080;
    }

    server {
        listen 80;

        location / {
            root /usr/share/nginx/html;
            index index.html;
            try_files $uri $uri/ /index.html;
        }

        location /api/ {
            proxy_pass http://backend_cluster;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}

---

Imágen de la interfaz

<img width="1151" height="579" alt="image" src="https://github.com/user-attachments/assets/d175da70-e14e-4a5c-a906-0d73497220c8" />

Autor: Arley Galindo Forero

Fecha: Noviembre 2025
Versión: 1.0
