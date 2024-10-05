package com.chimera.weapp.controller;

import com.alibaba.fastjson2.JSONObject;
import com.chimera.weapp.dto.LoginDTO;
import com.chimera.weapp.dto.ResponseBodyDTO;
import com.chimera.weapp.dto.UserDTO;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.service.SecurityService;
import com.chimera.weapp.service.WeChatService;
import com.chimera.weapp.util.JwtUtils;
import com.chimera.weapp.util.PasswordUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository repository;
    @Autowired
    private WeChatService weChatService;
    @Autowired
    private SecurityService securityService;

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<ResponseBodyDTO<UserDTO>> login(@RequestBody LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(new ResponseBodyDTO<>("用户名或密码为空", null));
        }

        Optional<User> user = repository.findByName(username);
        if (user.isEmpty()) {
            return new ResponseEntity<>(new ResponseBodyDTO<>("根据所输用户名找不到用户", null), HttpStatus.NOT_FOUND);
        } else {
            User user1 = user.get();
            boolean succeed = PasswordUtils.checkPassword(password, user1.getHashedPassword());
            if (succeed) {
                String token = JwtUtils.generateToken(
                        user1.getId().toHexString(),
                        user1.getName(),
                        user1.getRole(), null);
                HttpHeaders headers = new HttpHeaders();

                headers.add("Authorization", "Bearer " + token);
                user1.setJwt(token);
                User save = repository.save(user1);
                log.info("用户：{} 登录成功", user1.getName());
                return new ResponseEntity<>(new ResponseBodyDTO<>("登陆成功", UserDTO.ofUser(save).build()), headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBodyDTO<>("用户名和密码不匹配", null), HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @GetMapping("/wx")
    @Transactional
    public ResponseEntity<ResponseBodyDTO<UserDTO>> wxLoginOrRegister(@RequestParam(value = "code") String code) throws IOException, URISyntaxException {
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseBodyDTO<>("code为空", null));
        }
        JSONObject session = weChatService.code2session(code);
        String openid = session.getString("openid");
        String sessionKey = session.getString("session_key");
        Optional<User> user = repository.findByOpenid(openid);
        if (user.isEmpty()) {
            User newUser = User.builder()
                    .openid(openid)
                    .sessionKey(sessionKey)
                    .role(RoleEnum.CUSTOMER.toString())
                    .name("wx_" + UUID.randomUUID()).build();
            User save = repository.save(newUser);
            String jwt = JwtUtils.generateToken(save.getId().toHexString(), save.getName(), save.getRole(), save.getOpenid());
            save.setJwt(jwt);
            repository.save(save);
            HttpHeaders headers = new HttpHeaders();

            headers.add("Authorization", "Bearer " + jwt);
            log.info("小程序用户：{} 注册成功", save.getName());
            return new ResponseEntity<>(
                    new ResponseBodyDTO<>("自动注册成功",
                            UserDTO.ofUser(save).build()), headers, HttpStatus.OK);
        } else {
            User user1 = user.get();
            String jwt = user1.getJwt();
            try {
                Claims claims = JwtUtils.parseToken(jwt);
                securityService.tryToRefreshToken(claims);
            } catch (ExpiredJwtException e) {
                jwt = JwtUtils.generateToken(user1.getId().toHexString(), user1.getName(), user1.getRole(), user1.getOpenid());
                user1.setJwt(jwt);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + jwt);
            return new ResponseEntity<>(new ResponseBodyDTO<>("登陆成功",
                    UserDTO.ofUser(repository.findByOpenid(openid).orElseThrow()).build()),
                    headers, HttpStatus.OK);
        }
    }

}