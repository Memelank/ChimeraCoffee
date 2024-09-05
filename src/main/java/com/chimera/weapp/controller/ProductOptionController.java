package com.chimera.weapp.controller;

import com.chimera.weapp.entity.ProductOption;
import com.chimera.weapp.repository.ProductOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/productOption")
public class ProductOptionController {
    @Autowired
    private ProductOptionRepository repository;

    @PostMapping
    public ResponseEntity<ProductOption> createEntity(@RequestBody ProductOption entity) {
        return ResponseEntity.ok(repository.save(entity));
    }
    @GetMapping
    public List<ProductOption> getAllEntities() {
        List<ProductOption> all = repository.findAll();
        return all;
    }

}
