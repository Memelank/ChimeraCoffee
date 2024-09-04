package com.chimera.weapp.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.chimera.weapp.entity.User;
import java.util.List;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    List<User> findByName(String name);
}