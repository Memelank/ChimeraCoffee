package com.chimera.weapp.controller;

import com.alibaba.fastjson2.JSONObject;
import com.chimera.weapp.dto.LoginDTO;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.service.SecurityService;
import com.chimera.weapp.service.WeChatService;
import com.chimera.weapp.util.JSONUtils;
import com.chimera.weapp.util.JwtUtils;
import com.chimera.weapp.util.PasswordUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
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
    @Operation(summary = "若成功，可返回的json中key为data的id，openid，name字段取相应值")
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
                        user1.getRole(), null);
                HttpHeaders headers = new HttpHeaders();

                headers.add("Authorization", "Bearer " + token);
                user1.setJwt(token);
                User save = repository.save(user1);
                log.info("用户：{} 登录成功", user1.getName());
                String responseBody = JSONUtils.buildResponseBody("登陆成功", buildResponseDataValue(save));
                return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("用户名和密码不匹配", HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @PostMapping("/wx")
    @Transactional
    @Operation(summary = "若成功，可返回的json中key为data的id，openid，name字段取相应值")
    public ResponseEntity<String> wxLoginOrRegister(@RequestBody JSONObject requestBody) throws IOException, URISyntaxException, ParseException {
        String code = requestBody.getString("code");
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("code为空");
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
            String responseBody = JSONUtils.buildResponseBody("自动注册成功", buildResponseDataValue(save));
            return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
        } else {
            User user1 = user.get();
            String jwt = user1.getJwt();
            try {
                Claims claims = JwtUtils.parseToken(jwt);
                securityService.tryToRefreshToken(claims);
            }catch (ExpiredJwtException e){
                jwt = JwtUtils.generateToken(user1.getId().toHexString(), user1.getName(), user1.getRole(), user1.getOpenid());
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + jwt);
            String responseBody = JSONUtils.buildResponseBody("登录成功", buildResponseDataValue(user1));
            return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
        }
    }

    private Map<?, ?> buildResponseDataValue(User user) {
        ObjectId userId = user.getId();
        String openid = user.getOpenid();
        String name = user.getName();
        HashMap<Object, Object> map = new HashMap<>();
        map.put("id", userId.toHexString());
        map.put("openid", openid);
        map.put("name", name);
        return map;
    }
}