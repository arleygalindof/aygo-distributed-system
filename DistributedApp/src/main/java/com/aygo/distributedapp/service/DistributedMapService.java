package com.aygo.distributedapp.service;

import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DistributedMapService {

    private final Map<String, String> distributedMap = new ConcurrentHashMap<>();
    private final String serverId;

    // Lista simulada de nodos en el cluster
    private static final List<String> knownServers = new ArrayList<>();

    public DistributedMapService() {
        String hostname;

        try {
            // 1️⃣ Intentar obtener HOSTNAME de Docker
            hostname = System.getenv("HOSTNAME");

            // 2️⃣ Si es un hash (común en Docker), usar CONTAINER_NAME o IP
            if (hostname == null || hostname.isBlank() || hostname.matches("^[0-9a-f]{12,}$")) {
                String containerName = System.getenv("CONTAINER_NAME");
                if (containerName != null && !containerName.isBlank()) {
                    hostname = containerName;
                } else {
                    hostname = InetAddress.getLocalHost().getHostAddress();
                }
            }

            // 3️⃣ Fallback
            if (hostname == null || hostname.isBlank()) {
                hostname = "Servidor-desconocido";
            }

        } catch (Exception e) {
            hostname = "Servidor-desconocido";
        }

        this.serverId = hostname + ":8080";

        // 🔁 Registrar este servidor en la lista global (simulando el cluster)
        synchronized (knownServers) {
            if (!knownServers.contains(this.serverId)) {
                knownServers.add(this.serverId);
            }
        }

        System.out.println("[DistributedMapService] Servidor identificado como: " + this.serverId);
    }

    // ---------- Métodos de negocio ----------

    public void put(String key, String value) {
        distributedMap.put(key, value);
    }

    public String get(String key) {
        return distributedMap.get(key);
    }

    public Map<String, String> all() {
        return distributedMap;
    }

    public String getServerId() {
        return serverId;
    }

    public List<String> getAvailableServers() {
        // Devuelve lista de servidores que han reportado actividad
        synchronized (knownServers) {
            return new ArrayList<>(knownServers);
        }
    }

    public String addEntry(String key, String value) {
        distributedMap.put(key, value);
        return String.format("✅ Clave '%s' agregada correctamente — procesado por %s", key, serverId);
    }
}
