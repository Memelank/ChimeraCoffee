package com.chimera.weapp.repository;

import com.chimera.weapp.entity.ProcessorMap;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessorMapRepository extends MongoRepository<ProcessorMap, ObjectId> {
}
