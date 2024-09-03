package com.chimera.weapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.chimera.weapp.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
}