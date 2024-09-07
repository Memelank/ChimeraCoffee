package com.chimera.weapp.aspect;

import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

@Component
@Aspect
@Slf4j
public class SecurityAspect {
    @Autowired
    private UserRepository repository;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @Pointcut("@annotation(com.chimera.weapp.annotation.LoginRequired)")
    public void onLoginRequired() {
    }

    @Pointcut("@annotation(com.chimera.weapp.annotation.RolesAllow)")
    public void onRolesAllow() {
    }


    // 认证切面，拦截 @LoginRequired 注解的方法
    @Around("onLoginRequired()")
    @Order(1)
    public Object checkLogin(ProceedingJoinPoint pjp) throws Exception {
        log.info("{} before checkLogin args are {}", pjp.getSignature().toString(), pjp.getArgs());
        boolean canRefresh = false;
        try {

            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return new ResponseEntity<>("Unauthorized - Token missing or malformed", HttpStatus.UNAUTHORIZED);
            }

            token = token.substring(7);  // 去掉 "Bearer " 前缀
            Claims claims = JwtUtils.parseToken(token);

            if (JwtUtils.isTokenExpired(token)) {
                return new ResponseEntity<>("Unauthorized - Token expired", HttpStatus.UNAUTHORIZED);
            }
            String username = (String) claims.get("userName");
            compareTokenWithMongoDBToken(token, username);

            // 存储用户信息，以便后续使用
            request.setAttribute("claims", claims);

            canRefresh = true;
            return pjp.proceed();

        } catch (Throwable e) {
            canRefresh = false;
            log.error("exception on method was not properly caught", e);
            return ResponseEntity
                    .internalServerError()
                    .body("服务器未知错误: " + e.getMessage());
        } finally {
            if (canRefresh) {
                Claims claims = (Claims) request.getAttribute("claims");

                Date now = new Date();
                Date expiration = claims.getExpiration();
                // 如果令牌即将过期，刷新令牌
                if (expiration.getTime() - now.getTime() < 60 * 60 * 1000) {  // 如果距离过期少于60分钟
                    String userId = (String) claims.get("userId");
                    String userName = (String) claims.get("userName");
                    String role = (String) claims.get("role");
                    String newToken = JwtUtils.generateToken(userId, userName, role);

                    response.setHeader("Authorization", "Bearer " + newToken);

                    User user = repository.findById(new ObjectId(userId))
                            .orElseThrow(() -> new Exception("user did not found when refresh"));
                    user.setJwt(newToken);
                    repository.save(user);
                }
            }

        }

    }

    private void compareTokenWithMongoDBToken(String token, String username) throws Exception {
        User user = repository.findByName(username).orElseThrow(() -> new Exception("user not found"));
        String jwt = user.getJwt();
        if (jwt == null || jwt.isEmpty()) {
            throw new Exception("Unauthorized - User had log out");
        }
        if (!token.equals(jwt)) {
            throw new Exception("Unauthorized - Token is different");
        }
    }


    // 鉴权切面，拦截 @RoleAllow 注解的方法
    @Around("onRolesAllow()")
    @Order(2)
    public Object checkRole(ProceedingJoinPoint pjp) throws Throwable {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return new ResponseEntity<>("Unauthorized - No claims found", HttpStatus.UNAUTHORIZED);
        }

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        RolesAllow rolesAllow = method.getAnnotation(RolesAllow.class);
        String role = (String) claims.get("role");
        boolean hasRequiredRole = Arrays.stream(rolesAllow.value())
                .map(Enum::name)
                .anyMatch(roleAllow -> roleAllow.equals(role));


        if (!hasRequiredRole) {
            return new ResponseEntity<>(String.format("Forbidden - Insufficient role,your role: %s", role), HttpStatus.FORBIDDEN);
        }
        return pjp.proceed();
    }
}

