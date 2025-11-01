package com.aygo.loadbalancer.controller;

import com.aygo.loadbalancer.service.RoundRobinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/api")
public class ProxyController {

    private final RoundRobinService roundRobinService;
    private final WebClient webClient = WebClient.create();

    @Autowired
    public ProxyController(RoundRobinService roundRobinService) {
        this.roundRobinService = roundRobinService;
    }

    @GetMapping("/map")
    public Object getMap() {
        String server = roundRobinService.nextServer();
        return webClient.get()
                .uri(server + "/api/map")
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    @GetMapping("/map/add")
    public ResponseEntity<String> addEntry(
        @RequestParam(name = "key") String key,
        @RequestParam(name = "value") String value) {
        String server = roundRobinService.nextServer();
        String url = server + "/api/map/add?key=" + key + "&value=" + value;
        System.out.println("📡 Redirigiendo petición a: " + url);

        try {
            String result = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok("Nodo [" + server + "]: " + result);
        } catch (Exception e) {
            System.err.println("❌ Error al conectar con el nodo " + server + ": " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error al conectar con el nodo " + server);
        }
    }


    @GetMapping("/servers")
    public ResponseEntity<Object> listServers() {
        return ResponseEntity.ok(roundRobinService.getBackends());
    }
}
