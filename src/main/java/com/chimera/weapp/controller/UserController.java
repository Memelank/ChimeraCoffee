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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(repository.findAllNonAdminUsers());
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

    @GetMapping("/new")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<List<User>> getNewUsers(
            @RequestParam("startTime") String startTimeStr,
            @RequestParam("endTime") String endTimeStr
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);

            List<User> newUsers = repository.findUsersByCreatedAtBetween(startTime, endTime, RoleEnum.ADMIN);
            return ResponseEntity.ok(newUsers);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}

