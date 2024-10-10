package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Product;
import com.chimera.weapp.entity.ProductCate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.bson.types.ObjectId;

import java.util.List;

public interface ProductCateRepository extends MongoRepository<ProductCate, ObjectId> {
    List<ProductCate> findAllByDeleteIs(int delete);
    List<ProductCate> findAllByDeleteIsAndStatusIs(int delete, int status);
}