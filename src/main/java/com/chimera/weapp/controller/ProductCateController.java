package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.Product;
import com.chimera.weapp.entity.ProductCate;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.ProductCateRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
    @Operation(summary = "获得所有ProductCates，用于菜单侧边栏分类")
    public ResponseEntity<List<ProductCate>> getAllProductCates() {
        return ResponseEntity.ok(repository.findAllByDeleteIsAndStatusIs(0, 1));
    }

    // 获取所有产品类别
    @GetMapping("/shop")
    public ResponseEntity<List<ProductCate>> getAllProductCatesShop() {
        return ResponseEntity.ok(repository.findAllByDeleteIs(0));
    }

    // 创建新产品类别
    @PostMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<ProductCate> createProductCate(@RequestBody ProductCate entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    // 更新产品类别信息
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<ProductCate> updateProductCate(@RequestBody ProductCate updatedProductCate) {
        return ResponseEntity.ok(repository.save(updatedProductCate));
    }

}
