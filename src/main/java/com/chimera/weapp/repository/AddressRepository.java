package com.chimera.weapp.repository;

import com.chimera.weapp.entity.Address;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AddressRepository extends MongoRepository<Address, ObjectId> {
    List<Address> findBySchool(String school);
}
