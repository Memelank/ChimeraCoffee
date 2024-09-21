package com.chimera.weapp.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.chimera.weapp.entity.User;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByName(String name);
    Optional<User> findByOpenid(String openId);

    @Query("{ 'role': { $ne: 'ADMIN' } }")
    List<User> findAllNonAdminUsers();
}