package com.chimera.weapp.repository;

import com.chimera.weapp.entity.ProductOption;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductOptionRepository extends MongoRepository<ProductOption, ObjectId> {
}
