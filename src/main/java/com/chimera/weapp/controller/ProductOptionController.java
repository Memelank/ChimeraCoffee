package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.Product;
import com.chimera.weapp.entity.ProductOption;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.ProductOptionRepository;
import com.chimera.weapp.repository.ProductRepository;
import com.chimera.weapp.vo.OptionValue;
import io.swagger.v3.oas.annotations.Operation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productOption")
public class ProductOptionController {
    @Autowired
    private ProductOptionRepository repository;

    @Autowired
    private ProductRepository productRepository;

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
    @Operation(summary = "获得所有ProductOptions，用于选择商品具体选项，如：“规格”，“温度”")
    public List<ProductOption> getAllProductOptions() {
        List<ProductOption> all = repository.findAll();
        return all;
    }

    @DeleteMapping(value = "/{id}")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<String> deleteProductOption(@PathVariable("id") String id) {
        // Check if any product (delete = 0) is using this product option
        List<Product> products = productRepository.findByDelete(0);
        for (Product product : products) {
            Map<String, List<OptionValue>> productOptions = product.getProductOptions();
            if (productOptions != null && productOptions.containsKey(id)) {
                return ResponseEntity.badRequest().body("Product option is still in use, deletion failed.");
            }
        }

        // If no product is using this option, delete it
        repository.deleteById(new ObjectId(id));
        return ResponseEntity.ok("Product option deleted successfully.");
    }

}
