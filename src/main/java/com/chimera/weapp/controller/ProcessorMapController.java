package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.ProcessorMap;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.ProcessorMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@LoginRequired
@RolesAllow({RoleEnum.ADMIN})
@RestController
@RequestMapping("/processorMap")
public class ProcessorMapController {
    @Autowired
    private ProcessorMapRepository repository;

    @PostMapping
    public ResponseEntity<ProcessorMap> createProcessorMap(@RequestBody ProcessorMap entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @GetMapping
    public ResponseEntity<List<ProcessorMap>> getAllProcessorMaps() {
        return ResponseEntity.ok(repository.findAll());
    }
}
