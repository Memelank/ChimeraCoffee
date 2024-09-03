package com.chimera.weapp.controller;

import com.chimera.weapp.entity.Product;
import com.chimera.weapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductRepository repository;

    @GetMapping
    public List<Product> getAllEntities() {
        return repository.findAll();
    }

    @PostMapping
    public Product createEntity(@RequestBody Product entity) {
        return repository.save(entity);
    }
}
