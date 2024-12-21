package com.chimera.weapp.repository;

import com.chimera.weapp.entity.OperationRecord;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface OperationRecordRepository extends MongoRepository<OperationRecord, ObjectId> {
    List<OperationRecord> findByInventoryId(ObjectId inventoryId);
    // 在 OperationRecordRepository 接口中添加
    List<OperationRecord> findByTimestampBetween(Date start, Date end);

}
