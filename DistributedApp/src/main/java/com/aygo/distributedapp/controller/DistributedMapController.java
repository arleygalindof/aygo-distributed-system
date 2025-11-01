package com.aygo.distributedapp.controller;

import com.aygo.distributedapp.service.DistributedMapService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/map")
public class DistributedMapController {

    private final DistributedMapService mapService;

    public DistributedMapController(DistributedMapService mapService) {
        this.mapService = mapService;
    }

    @PostMapping
    public String put(@RequestParam("key") String key, @RequestParam("value") String value) {
        mapService.put(key, value);
        return "OK";
    }

    @GetMapping("/{key}")
    public String get(@PathVariable String key) {
        String value = mapService.get(key);
        if (value == null) {
            return "(no encontrado)";
        }
        return value;
    }

    @GetMapping
    public Map<String, String> getAll() {
        return mapService.all();
    }

    @GetMapping("/add")
    public String putFromGet(@RequestParam("key") String key, @RequestParam("value") String value) {
        mapService.put(key, value);
        return "OK (agregado desde GET) — procesado por " + mapService.getServerId();
    }

}
