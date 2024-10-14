package com.chimera.weapp.repository;

import com.chimera.weapp.entity.AppConfiguration;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AppConfigurationRepository extends MongoRepository<AppConfiguration, ObjectId> {
    // 根据键名查询配置
    String CONTACT_PHONE_NUMBER = "contact_phone_number";
    String WXTS = "wxts";
    String TEMPLATE_ID = "templateID";
    String SHOP_NAME = "shopName";
    String ADDRESS = "address";
    String PAGE = "page";
    Optional<AppConfiguration> findByKey(String key);
    Optional<AppConfiguration> findByKeyAndCategory(String key, String category);

}
