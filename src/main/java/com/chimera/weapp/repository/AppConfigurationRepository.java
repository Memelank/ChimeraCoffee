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
    String SHOP_ADDRESS = "shop_address";
    String PAGE = "page";
    String PERIODICALLY_SEND_EVENT_SWITCH="periodically_send_event_switch";
    String PERIODICALLY_SEND_EVENT_START_TIME="periodically_send_event_start_time";
    String PERIODICALLY_SEND_EVENT_END_TIME="periodically_send_event_end_time";
    String THE_PERIOD_OF_TIME="the_period_of_time";
    Optional<AppConfiguration> findByKey(String key);
    Optional<AppConfiguration> findByKeyAndCategory(String key, String category);

}
