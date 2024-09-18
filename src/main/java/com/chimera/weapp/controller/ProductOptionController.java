package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.Product;
import com.chimera.weapp.entity.ProductOption;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.ProductOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/productOption")
public class ProductOptionController {
    @Autowired
    private ProductOptionRepository repository;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<ProductOption> createProductOption(@RequestBody ProductOption entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<ProductOption> updateProductOption(@RequestBody ProductOption entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @GetMapping
    public List<ProductOption> getAllProductOptions() {
        List<ProductOption> all = repository.findAll();
        return all;
    }

}
