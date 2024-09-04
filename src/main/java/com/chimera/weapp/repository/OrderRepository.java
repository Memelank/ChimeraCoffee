package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, ObjectId> {
}
