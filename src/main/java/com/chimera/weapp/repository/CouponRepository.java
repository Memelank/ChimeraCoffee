package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Coupon;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CouponRepository extends MongoRepository<Coupon, ObjectId> {
}
