package com.chimera.weapp.repository;

import com.chimera.weapp.enums.RoleEnum;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.chimera.weapp.entity.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByName(String name);
    Optional<User> findByOpenid(String openId);

    @Query("{ 'role': { $ne: 'ADMIN' } }")
    List<User> findAllNonAdminUsers();

    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 }, 'role': { $ne: ?2 } }")
    List<User> findUsersByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, RoleEnum adminRole);

    List<User> findByStudentCert(boolean studentCert);

}