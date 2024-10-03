package com.chimera.weapp.repository;

import com.chimera.weapp.entity.FixDeliveryInfo;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FixDeliveryInfoRepository extends MongoRepository<FixDeliveryInfo, ObjectId> {
    List<FixDeliveryInfo> findBySchool(String school);
}
