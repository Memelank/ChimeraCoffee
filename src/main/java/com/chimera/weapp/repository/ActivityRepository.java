package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Activity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ActivityRepository extends MongoRepository<Activity, ObjectId> {
}
