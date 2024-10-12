package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import java.util.Date;

public interface OrderRepository extends MongoRepository<Order, ObjectId> {
    List<Order> findByUserIdOrderByCreatedAtDesc(ObjectId userId);
    List<Order> findTop10ByUserIdOrderByCreatedAtDesc(ObjectId userId);
    long countByCreatedAtGreaterThanEqual(Date startOfDay);
    List<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(Date startTime, Date endTime);
}
