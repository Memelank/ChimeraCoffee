package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Inventory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface InventoryRepository extends MongoRepository<Inventory, ObjectId> {
    Optional<Inventory> findByIdAndDeletedFalse(ObjectId id);

}
