package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Activity;
import com.chimera.weapp.entity.Product;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Date;

public interface ActivityRepository extends MongoRepository<Activity, ObjectId> {
    List<Activity> findAllByDeleteIs(int delete);
    List<Activity> findAllByDeleteIsAndStatusIs(int delete, int status);
    List<Activity> findAllByStatusIsAndEndTimeBefore(int status, Date now);
}
