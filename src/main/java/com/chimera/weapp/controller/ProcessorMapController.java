package com.chimera.weapp.controller;

import com.chimera.weapp.entity.ProcessorMap;
import com.chimera.weapp.repository.ProcessorMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/processorMap")
public class ProcessorMapController {
    @Autowired
    private ProcessorMapRepository repository;

    @PostMapping
    public ResponseEntity<ProcessorMap> createEntity(@RequestBody ProcessorMap entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

}
