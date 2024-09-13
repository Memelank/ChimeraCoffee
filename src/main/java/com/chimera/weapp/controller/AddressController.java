package com.chimera.weapp.controller;

import com.chimera.weapp.entity.Address;
import com.chimera.weapp.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    private AddressRepository repository;

    @GetMapping
    public List<Address> getAllAddresses() {
        return repository.findAll();
    }

    @PutMapping
    public ResponseEntity<Address> updateAddress(@RequestBody Address user) {
        return ResponseEntity.ok(repository.save(user));
    }

    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody Address entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @GetMapping("/{school}")
    public ResponseEntity<List<Address>> getAddressByName(@PathVariable String school) {
        return ResponseEntity.ok(repository.findBySchool(school));
    }
}

