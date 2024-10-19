package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.dto.UserDTO;
import com.chimera.weapp.entity.Coupon;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.CouponRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.service.BenefitService;
import com.chimera.weapp.service.SecurityService;
import com.chimera.weapp.util.ThreadLocalUtil;
import com.chimera.weapp.vo.CouponIns;
import com.chimera.weapp.vo.PointsProductIns;
import io.swagger.v3.oas.annotations.Operation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponRepository repository;

    @Autowired
    private BenefitService benefitService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(repository.findAllByDeleteIs(0));
    }

    @GetMapping("/for_points")
    @LoginRequired
    @Operation(summary = "获取积分兑换优惠券列表,在\"我的-积分\"页面使用")
    public ResponseEntity<List<Coupon>> getAllCouponsForPoints() {
        return ResponseEntity.ok(repository.findAllByConvertibleIs(true));
    }

    @Operation(summary = "给某个顾客发金条，商铺端专用")
    @PostMapping("/add_to_user")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<?> addCouponToUser(@RequestParam String userId, @RequestParam String couponId) {
        try {
            benefitService.addCouponToUser(userId, couponId);
            return ResponseEntity.ok("Coupon added to user successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Coupon> updateCoupon(@RequestBody Coupon entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @PostMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @GetMapping("/current_user")
    @LoginRequired
    @Operation(summary = "获取当前用户的所有优惠券实例")
    public ResponseEntity<List<CouponIns>> getCouponInsOfAUser() {
        UserDTO userDTO = ThreadLocalUtil.get(ThreadLocalUtil.USER_DTO, UserDTO.class);
        String id = userDTO.getId();
        User user = userRepository.findById(new ObjectId(id)).orElseThrow();
        return ResponseEntity.ok(user.getCoupons());
    }

//    @DeleteMapping("/{id}")
//    @LoginRequired
//    @RolesAllow(RoleEnum.ADMIN)
//    public ResponseEntity<Void> deleteCoupon(@PathVariable String id) {
//        ObjectId objectId;
//        try {
//            objectId = new ObjectId(id);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().build(); // 返回400错误，表示无效的ObjectId
//        }
//
//        if (!repository.existsById(objectId)) {
//            return ResponseEntity.notFound().build();
//        }
//
//        repository.deleteById(objectId);
//        return ResponseEntity.noContent().build();
//    }

}
