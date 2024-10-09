package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.Coupon;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.CouponRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponRepository repository;

    @GetMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public List<Coupon> getAllCoupons() {
        return repository.findAll();
    }

    @PutMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Coupon> updateCoupon(@RequestBody Coupon entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @PostMapping
    @LoginRequired
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Void> deleteCoupon(@PathVariable String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 返回400错误，表示无效的ObjectId
        }

        if (!repository.existsById(objectId)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(objectId);
        return ResponseEntity.noContent().build();
    }

}
