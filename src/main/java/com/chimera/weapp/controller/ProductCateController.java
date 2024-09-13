package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.ProductCate;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.ProductCateRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product_cates")
public class ProductCateController {

    @Autowired
    private ProductCateRepository repository;

    // 获取所有产品类别
    @GetMapping
    public List<ProductCate> getAllProductCates() {
        return repository.findAll();
    }

    // 创建新产品类别
    @PostMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<ProductCate> createProductCate(@RequestBody ProductCate entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    // 根据ID获取产品类别
    @GetMapping("/{id}")
    public ResponseEntity<ProductCate> getProductCate(@PathVariable String id) {
        return repository.findById(new ObjectId(id))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 更新产品类别信息
    @PutMapping("/{id}")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<ProductCate> updateProductCate(@PathVariable String id, @RequestBody ProductCate updatedProductCate) {
        return repository.findById(new ObjectId(id))
                .map(productCate -> {
                    // 仅更新提供的新值，保留原有数据
                    if (updatedProductCate.getTitle() != null) {
                        productCate.setTitle(updatedProductCate.getTitle());
                    }
                    if (updatedProductCate.getStatus() != null) {
                        productCate.setStatus(updatedProductCate.getStatus());
                    }
                    // 可以添加更多字段的判断和更新逻辑
                    return ResponseEntity.ok(repository.save(productCate));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    // 删除产品类别
    @DeleteMapping("/{id}")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Void> deleteProductCate(@PathVariable String id) {
        if (!repository.existsById(new ObjectId(id))) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(new ObjectId(id));
        return ResponseEntity.noContent().build();
    }
}
