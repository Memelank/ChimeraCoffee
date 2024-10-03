package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

public interface OrderRepository extends MongoRepository<Order, ObjectId> {
    long countByCreatedAtGreaterThanEqual(Date startOfDay);
}
