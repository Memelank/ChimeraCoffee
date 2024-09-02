package com.chimera.weapp.repository;

import com.chimera.weapp.entity.ProcessorMap;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ProcessorRepository extends MongoRepository<ProcessorMap, ObjectId> {
    @Query(value = "{ 'state' : ?0,'event':?1,'businessType':?2,'scenes':?3 }", fields = "{ 'processorIds' : 1 }")
    int[] findProcessorIds(String state, String event, String businessType, String scene);
    //TODO 不知道会不会返回null，要查文档
}
