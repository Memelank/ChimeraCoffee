package com.chimera.weapp.controller;

import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{uid}")
    public User getEntityByName(@PathVariable String uid) {
        return repository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + uid));
    }

    // 更新用户信息
    @PutMapping("/{uid}")
    public ResponseEntity<User> updateUser(@PathVariable String uid, @RequestBody User updatedUser) {
        return repository.findById(uid)
                .map(user -> {
                    // 仅更新提供的新值，保留原有数据
                    if (updatedUser.getName() != null) {
                        user.setName(updatedUser.getName());
                    }
                    if (updatedUser.getSchool() != null) {
                        user.setSchool(updatedUser.getSchool());
                    }
                    return ResponseEntity.ok(repository.save(user));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    // 删除用户
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> deleteUser(@PathVariable String uid) {
        if (!repository.existsById(uid)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(uid);
        return ResponseEntity.noContent().build();
    }
}

