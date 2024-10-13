package com.chimera.weapp.repository;

import com.chimera.weapp.entity.AppConfiguration;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AppConfigurationRepository extends MongoRepository<AppConfiguration, ObjectId> {
    // 根据键名查询配置
    Optional<AppConfiguration> findByKey(String key);
}
