package com.chimera.weapp.controller;

import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository repository;

    @GetMapping
    public List<User> getAllEntities() {
        return repository.findAll();
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        return ResponseEntity.ok(repository.save(user));
    }

    @PostMapping
    public ResponseEntity<User> createEntity(@RequestBody User entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @GetMapping("/{name}")
    public ResponseEntity<List<User>> getEntityByName(@PathVariable String name) {
        return ResponseEntity.ok(repository.findByName(name));
    }
}

