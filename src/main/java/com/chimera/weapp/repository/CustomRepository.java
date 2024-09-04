package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.statemachine.enums.OrderStateEnum;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Integer> findProcessorIds(String state, String event, String businessType, String scene) {
        Query query = new Query();
        query.addCriteria(Criteria.where("state").is(state)
                .and("event").is(event)
                .and("businessTypes").is(businessType)
                .and("scenes").is(scene));
        query.fields().include("processorIds").exclude("_id");

        // 返回 processorIds 字段
        return mongoTemplate.findDistinct(query, "processorIds", "processor_map", Integer.class);
    }

    public void updateOrderStateById(String id, String state) {
        // 构建查询条件
        Query query = new Query(Criteria.where("id").is(id));

        // 构建更新操作
        Update update = new Update();
        update.set("state", state);
        mongoTemplate.updateMulti(query, update, Order.class);
    }
}
