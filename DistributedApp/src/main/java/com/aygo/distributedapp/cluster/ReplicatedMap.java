package com.aygo.distributedapp.cluster;

import org.jgroups.*;
import org.jgroups.util.Util;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapa replicado entre todos los nodos del clúster usando JGroups.
 * Permite operaciones de put/get/remove con replicación automática.
 */
@Component
public class ReplicatedMap implements Receiver {

    private JChannel channel;
    private final Map<String, String> map = new HashMap<>();
    private volatile View currentView; // 🔹 Mantiene vista actual del clúster

    @PostConstruct
    public void init() {
        Thread clusterThread = new Thread(() -> {
            try {
                channel = new JChannel("jgroups.xml");
                channel.setReceiver(this);
                channel.connect("ReplicatedMapCluster");
                channel.getState(null, 10000);
                System.out.println("[JGroups] Canal conectado al clúster ReplicatedMapCluster");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        clusterThread.setDaemon(true);
        clusterThread.start();

        // 🔹 Espera activa hasta que el canal esté conectado
        int attempts = 0;
        while ((channel == null || !channel.isConnected()) && attempts < 20) {
            try {
                Thread.sleep(500);
                attempts++;
            } catch (InterruptedException ignored) {}
        }

        if (channel != null && channel.isConnected()) {
            System.out.println("[JGroups] Canal conectado correctamente tras " + attempts + " intentos");
        } else {
            System.err.println("[JGroups] ❌ No se logró conectar el canal tras varios intentos");
        }
    }


    private void broadcastUpdate(String action, String key, String value) throws Exception {
        if (channel == null || !channel.isConnected()) {
            throw new IllegalStateException("El canal de JGroups no está inicializado o no está conectado.");
        }
        String payload = action + "|" + key + "|" + (value == null ? "" : value);
        Message msg = new ObjectMessage(null, payload);
        channel.send(msg);
    }

    @Override
    public void receive(Message msg) {
        try {
            String payload = msg.getObject();
            String[] parts = payload.split("\\|");
            String action = parts[0];
            String key = parts[1];
            String value = parts.length > 2 ? parts[2] : null;

            switch (action) {
                case "put" -> map.put(key, value);
                case "remove" -> map.remove(key);
            }

            System.out.println("[Actualización recibida de " + msg.src() + "] " + payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (map) {
            Util.objectToStream(map, new DataOutputStream(output));
        }
        System.out.println("[Estado enviado]");
    }

    @Override
    public void setState(InputStream input) throws Exception {
        Map<String, String> received = Util.objectFromStream(new DataInputStream(input));
        synchronized (map) {
            map.clear();
            map.putAll(received);
        }
        System.out.println("[Estado recibido] Mapa actual: " + map);
    }

    @Override
    public void viewAccepted(View view) {
        this.currentView = view;
        System.out.println("Vista actualizada: " + view);
    }

    // 🔹 Métodos públicos para el servicio REST
    public void put(String key, String value) throws Exception {
        map.put(key, value);
        broadcastUpdate("put", key, value);
    }

    public String get(String key) {
        return map.get(key);
    }

    public Map<String, String> snapshot() {
        synchronized (map) {
            return new HashMap<>(map);
        }
    }

    public List<String> getClusterMembers() {
        try {
            if (channel == null || !channel.isConnected()) {
                return List.of("Canal no conectado");
            }
            return channel.getView().getMembers()
                    .stream()
                    .map(Object::toString)
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error obteniendo miembros del clúster");
        }    
    }
}
