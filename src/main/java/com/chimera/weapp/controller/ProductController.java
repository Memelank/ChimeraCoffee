package com.chimera.weapp.controller;

import com.chimera.weapp.entity.Product;
import com.chimera.weapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductRepository repository;

    @GetMapping
    public ResponseEntity<List<Product>> getAllEntities() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PutMapping
    public ResponseEntity<Product> updateEntity(@RequestBody Product entity) {
        return ResponseEntity.ok(repository.save(entity));
    }


    @PostMapping
    public ResponseEntity<Product> createEntity(@RequestBody Product entity) {
        return ResponseEntity.ok(repository.save(entity));
    }
}
