package com.chimera.weapp.controller;

import com.alibaba.fastjson2.JSONObject;
import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.dto.LoginDTO;
import com.chimera.weapp.dto.ResponseBodyDTO;
import com.chimera.weapp.dto.UserDTO;
import com.chimera.weapp.dto.WxStudentCheckDTO;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.service.BenefitService;
import com.chimera.weapp.service.SecurityService;
import com.chimera.weapp.service.WeChatRequestService;
import com.chimera.weapp.util.JwtUtils;
import com.chimera.weapp.util.PasswordUtils;
import com.chimera.weapp.util.ThreadLocalUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
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
    private WeChatRequestService weChatRequestService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private BenefitService benefitService;

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<ResponseBodyDTO<UserDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
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
                String token = JwtUtils.generateToken(user1.getId().toHexString());
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
    public ResponseEntity<ResponseBodyDTO<UserDTO>> wxLoginOrRegister(@RequestParam(value = "code") String code) throws Exception {
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseBodyDTO<>("code为空", null));
        }
        JSONObject session = weChatRequestService.code2session(code);
        String openid = session.getString("openid");
        String sessionKey = session.getString("session_key");
        Optional<User> userOptional = repository.findByOpenid(openid);
        if (userOptional.isEmpty()) {
            User newUser = User.builder()
                    .openid(openid)
                    .sessionKey(sessionKey)
                    .role(RoleEnum.CUSTOMER.toString())
                    .name("wx_" + UUID.randomUUID()).build();
            User save1 = repository.save(newUser);
            String jwt = JwtUtils.generateToken(save1.getId().toHexString());
            save1.setJwt(jwt);
            User save2 = repository.save(save1);
            User save3 = benefitService.issueNewCustomerCouponsToUser(save2.getId());
            User save4 = benefitService.issueActivityCouponsToUser(save3.getId());

            HttpHeaders headers = new HttpHeaders();

            headers.add("Authorization", "Bearer " + jwt);
            log.info("小程序用户:{} 注册成功,openid:{}", save4.getName(), save4.getOpenid());
            return new ResponseEntity<>(
                    new ResponseBodyDTO<>("自动注册成功",
                            UserDTO.ofUser(save4).build()), headers, HttpStatus.OK);
        } else {
            User user = userOptional.get();
            String jwt = user.getJwt();
            try {
                JwtUtils.parseToken(jwt);
            } catch (ExpiredJwtException e) {
                log.info(String.format("有小程序用户在jwt过期后重新登录，用户openid为%s", user.getOpenid()), e);
            }
            jwt = JwtUtils.generateToken(user.getId().toHexString());
            HttpHeaders headers = new HttpHeaders();
            user.setJwt(jwt);
            User save = repository.save(user);
            User save2 = benefitService.issueActivityCouponsToUser(save.getId());

            headers.add("Authorization", "Bearer " + jwt);
            return new ResponseEntity<>(new ResponseBodyDTO<>("登陆成功",
                    UserDTO.ofUser(save2).build()),
                    headers, HttpStatus.OK);
        }
    }

    @GetMapping("/validate")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    @Operation(description = "一个纯粹的管理员身份校验，输入请求头中的Authorization，输出响应头中的可能刷新的token，响应体中的用户基本信息")
    public ResponseEntity<ResponseBodyDTO<UserDTO>> validate() {
        UserDTO userDTO = ThreadLocalUtil.get(ThreadLocalUtil.USER_DTO, UserDTO.class);
        User user = repository.findById(new ObjectId(userDTO.getId())).orElseThrow();
        return new ResponseEntity<>(new ResponseBodyDTO<>("登陆成功",
                UserDTO.ofUser(user).build()), HttpStatus.OK);
    }

    @PostMapping("/wx/checkstudentidentity")
    @LoginRequired
    @Operation
    public ResponseEntity<WxStudentCheckDTO> checkStudentIdentity(@Valid CheckStudentIdentityApiParams apiParams) throws URISyntaxException, IOException {
        UserDTO userDTO = ThreadLocalUtil.get(ThreadLocalUtil.USER_DTO, UserDTO.class);
        String openid = userDTO.getOpenid();
        WxStudentCheckDTO wxStudentCheckDTO = weChatRequestService.checkStudentIdentity(
                WeChatRequestService.WxCheckStudentIdentityApiParams.builder()
                        .openid(openid)
                        .wx_studentcheck_code(apiParams.wx_student_check_code).build());
        int bindStatus = wxStudentCheckDTO.getBind_status();
        boolean student = wxStudentCheckDTO.is_student();
        if (bindStatus == 3 && student) {
            User user = repository.findByOpenid(openid).orElseThrow();
            user.setStudentCert(true);
            repository.save(user);
            log.info("用户{}成功认证", openid);
        }
        return ResponseEntity.ok(wxStudentCheckDTO);
    }


    public static class CheckStudentIdentityApiParams {
        @NotNull
        String wx_student_check_code;
    }
}