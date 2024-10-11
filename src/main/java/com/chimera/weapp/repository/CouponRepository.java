package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Coupon;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CouponRepository extends MongoRepository<Coupon, ObjectId> {
    List<Coupon> findAllByConvertibleIs(Boolean convertible);
    List<Coupon> findAllByDeleteIs(int delete);
}
