package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.vo.DeliveryInfo;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import java.util.Date;

public interface OrderRepository extends MongoRepository<Order, ObjectId> {
    List<Order> findByUserIdAndStateNotOrderByCreatedAtDesc(ObjectId userId, String state);
    List<Order> findTop10ByUserIdAndStateNotOrderByCreatedAtDesc(ObjectId userId, String state);
    List<Order> findTop1ByUserIdAndStateNotOrderByCreatedAtDesc(ObjectId userId, String state);
    long countByCreatedAtGreaterThanEqual(Date startOfDay);
    List<Order> findByCreatedAtBetweenAndStateNotOrderByCreatedAtDesc(Date startTime, Date endTime, String state);
    List<Order> findByDeliveryInfoSchoolAndDeliveryInfoAddressAndDeliveryInfoTimeAndState(
            String school, String address, Date date, String status);
}
