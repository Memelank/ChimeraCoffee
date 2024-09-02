package com.chimera.weapp.controller;

import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository repository;

    @GetMapping
    public List<User> getAllEntities() {
        return repository.findAll();
    }

    @PostMapping
    public User createEntity(@RequestBody User entity) {
        return repository.save(entity);
    }

    @GetMapping("/{name}")
    public List<User> getEntityByName(@PathVariable String name) {
        return repository.findByName(name);
    }
}

