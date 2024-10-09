package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Product;
import com.chimera.weapp.entity.ProductCate;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, ObjectId> {
    boolean existsByCateIdAndDelete(ObjectId cateId, int delete);
    List<Product> findAllByDeleteIs(int delete);
}
