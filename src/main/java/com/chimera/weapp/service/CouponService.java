package com.chimera.weapp.service;

import com.chimera.weapp.entity.Coupon;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.CouponRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.vo.CouponIns;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    public void addCouponToUser(String userId, String couponId) throws Exception {
        // Convert userId and couponId to ObjectId
        ObjectId userObjectId;
        ObjectId couponObjectId;
        try {
            userObjectId = new ObjectId(userId);
            couponObjectId = new ObjectId(couponId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid userId or couponId");
        }

        // Find the User
        Optional<User> userOptional = userRepository.findById(userObjectId);
        if (!userOptional.isPresent()) {
            throw new Exception("User not found");
        }
        User user = userOptional.get();

        // Find the Coupon
        Optional<Coupon> couponOptional = couponRepository.findById(couponObjectId);
        if (!couponOptional.isPresent()) {
            throw new Exception("Coupon not found");
        }
        Coupon coupon = couponOptional.get();

        // Check if the coupon is valid (status=1)
        if (coupon.getStatus() != 1) {
            throw new Exception("Coupon is not active");
        }

        // Check if the coupon is still valid (not expired)
        if (coupon.getValidity() != null && coupon.getValidity().before(new Date())) {
            throw new Exception("Coupon is expired");
        }

        // Create a new CouponIns
        CouponIns couponIns = CouponIns.builder()
                .uuid(UUID.randomUUID().toString())
                .couponId(coupon.getId().toHexString())
                .name(coupon.getName())
                .status(0) // 0=未使用
                .cateId(coupon.getCateId())
                .dePrice(coupon.getDePrice())
                .build();

        // Add the CouponIns to the User's coupons list
        if (user.getCoupons() == null) {
            user.setCoupons(new ArrayList<>());
        }
        user.getCoupons().add(couponIns);

        // Save the User
        userRepository.save(user);

        // Update the issueNum of the coupon
        coupon.setIssueNum(coupon.getIssueNum() + 1);
        couponRepository.save(coupon);
    }
}
