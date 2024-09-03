package com.chimera.weapp.controller;

import com.chimera.weapp.entity.Product;
import com.chimera.weapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.bson.types.ObjectId;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository repository;

    // 获取所有产品
    @GetMapping
    public List<Product> getAllEntities() {
        return repository.findAll();
    }

    // 创建新产品
    @PostMapping
    public Product createEntity(@RequestBody Product entity) {
        return repository.save(entity);
    }

    // 根据ID获取产品
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        return repository.findById(new ObjectId(id))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 更新产品信息
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product updatedProduct) {
        return repository.findById(new ObjectId(id))
                .map(product -> {
                    // 仅更新提供的新值，保留原有数据
                    if (updatedProduct.getCid() != null) {
                        product.setCid(updatedProduct.getCid());
                    }
                    if (updatedProduct.getTitle() != null) {
                        product.setTitle(updatedProduct.getTitle());
                    }
                    if (updatedProduct.getImg() != null) {
                        product.setImg(updatedProduct.getImg());
                    }
                    if (updatedProduct.getPrice() != null) {
                        product.setPrice(updatedProduct.getPrice());
                    }
                    if (updatedProduct.getDesc() != null) {
                        product.setDesc(updatedProduct.getDesc());
                    }
                    if (updatedProduct.getStatus() != null) {
                        product.setStatus(updatedProduct.getStatus());
                    }
                    if (updatedProduct.getAdd_date() != null) {
                        product.setAdd_date(updatedProduct.getAdd_date());
                    }
                    // 保存更新后的实体
                    return ResponseEntity.ok(repository.save(product));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 删除产品
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        if (!repository.existsById(new ObjectId(id))) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(new ObjectId(id));
        return ResponseEntity.noContent().build();
    }
}
