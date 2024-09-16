package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository repository;
    @Autowired
    private SecurityService securityService;


    @GetMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @PutMapping
    @LoginRequired
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        securityService.checkIdImitate(user.getId());
        return ResponseEntity.ok(repository.save(user));
    }

    @PostMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<User> createUser(@RequestBody User entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @GetMapping("/{name}")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<User> getUserByName(@PathVariable String name) {
        Optional<User> user = repository.findByName(name);
        return user.map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}

