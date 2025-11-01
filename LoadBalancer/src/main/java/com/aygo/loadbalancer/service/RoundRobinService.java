package com.aygo.loadbalancer.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoundRobinService {

    // Lista de nodos backend (puertos donde corren tus instancias)
    private final List<String> backends = List.of(
        "http://distributedapp1:8080",
        "http://distributedapp2:8080",
        "http://distributedapp3:8080"
    );

    private final AtomicInteger counter = new AtomicInteger(0);

    public String nextServer() {
        int index = Math.abs(counter.getAndIncrement() % backends.size());
        return backends.get(index);
    }

    public List<String> getBackends() {
        return backends;
    }
}
