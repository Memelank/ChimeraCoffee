package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.Address;
import com.chimera.weapp.enums.RoleEnum;
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
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public List<Address> getAllAddresses() {
        return repository.findAll();
    }

    @PutMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Address> updateAddress(@RequestBody Address entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @PostMapping
    @LoginRequired
    public ResponseEntity<Address> createAddress(@RequestBody Address entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

}

