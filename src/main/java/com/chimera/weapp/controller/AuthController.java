package com.chimera.weapp.controller;

import com.chimera.weapp.dto.LoginDTO;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.util.JwtUtils;
import com.chimera.weapp.util.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository repository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("缺少用户名或密码");
        }

        Optional<User> user = repository.findByName(username);
        if (user.isEmpty()) {
            return new ResponseEntity<>("根据所输用户名找不到用户", HttpStatus.NOT_FOUND);
        } else {
            User user1 = user.get();
            boolean succeed = PasswordUtils.checkPassword(password, user1.getHashedPassword());
            if (succeed) {
                String token = JwtUtils.generateToken(
                        user1.getId().toHexString(),
                        user1.getName(),
                        user1.getRole());
                HttpHeaders headers = new HttpHeaders();

                headers.add("Authorization", "Bearer " + token);
                user1.setJwt(token);
                repository.save(user1);
                log.info("用户：{} 登录成功", user1.getName());
                return new ResponseEntity<>("登陆成功", headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("用户名和密码不匹配", HttpStatus.UNAUTHORIZED);
            }
        }
    }
}
