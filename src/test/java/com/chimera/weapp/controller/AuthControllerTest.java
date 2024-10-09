package com.chimera.weapp.controller;

import com.alibaba.fastjson2.JSONObject;
import com.chimera.weapp.dto.ResponseBodyDTO;
import com.chimera.weapp.dto.UserDTO;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.service.SecurityService;
import com.chimera.weapp.service.WeChatService;
import com.chimera.weapp.util.JwtUtils;
import org.apache.hc.core5.http.ParseException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {
    @Mock
    private WeChatService weChatService;
    @Mock
    private UserRepository repository;
    @Mock
    private SecurityService securityService;
    @InjectMocks
    private AuthController authController;

    @Test
    public void wxLogin_register() throws IOException, URISyntaxException {
        MockitoAnnotations.openMocks(this);

        Mockito.when(weChatService.code2session(Mockito.any()))
                .thenReturn(JSONObject.parseObject("{\"openid\":\"openid666\",\"session_key\":\"session_key111\"}"));
        Mockito.when(repository.findByOpenid(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(repository.save(Mockito.any())).thenReturn(
                User.builder()
                        .id(new ObjectId(new Date()))
                        .openid("openid666")
                        .name("wx_customer1")
                        .role(RoleEnum.CUSTOMER.toString()).build());
        ResponseEntity<ResponseBodyDTO<UserDTO>> responseEntity = authController.wxLoginOrRegister("code");
        assertNotNull(responseEntity.getHeaders().get("Authorization"));
        assertTrue(responseEntity.getBody().getMsg().contains("自动注册成功"));
    }

    @Test
    public void wxLogin_Login() throws IOException, URISyntaxException {
        MockitoAnnotations.openMocks(this);

        Mockito.when(weChatService.code2session(Mockito.any()))
                .thenReturn(JSONObject.parseObject("{\"openid\":\"openid666\",\"session_key\":\"session_key111\"}"));
        Mockito.when(repository.findByOpenid(Mockito.any())).thenReturn(Optional.of(User.builder()
                .id(new ObjectId(new Date()))
                .openid("openid666")
                .name("wx_customer1")
                .jwt(JwtUtils.generateToken("id")).build()));
        ResponseEntity<ResponseBodyDTO<UserDTO>> responseEntity = authController.wxLoginOrRegister("code");
        assertTrue(responseEntity.getBody().getMsg().contains("登陆成功"));
    }
}