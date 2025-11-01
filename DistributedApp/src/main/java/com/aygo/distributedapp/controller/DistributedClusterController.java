package com.aygo.distributedapp.controller;

import com.aygo.distributedapp.service.DistributedMapService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/servers", "/servers"})
public class DistributedClusterController {

    private final DistributedMapService mapService;

    public DistributedClusterController(DistributedMapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping
    public List<String> getAvailableServers() {
        return mapService.getAvailableServers();
    }
}
