package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.bson.types.ObjectId;


public interface ProductRepository extends MongoRepository<Product, ObjectId> {
}