package com.chimera.weapp.repository;

import com.chimera.weapp.entity.PointsProduct;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PointsProductRepository extends MongoRepository<PointsProduct, ObjectId> {
    List<PointsProduct> findAllByDeleteIs(int delete);
    List<PointsProduct> findAllByDeleteIsAndStatusIs(int delete, int status);

}
