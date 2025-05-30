package com.chimera.weapp.aspect;

import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.dto.UserDTO;
import com.chimera.weapp.exception.CompareTokenException;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.service.SecurityService;
import com.chimera.weapp.util.JwtUtils;
import com.chimera.weapp.util.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
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

@Component
@Aspect
@Slf4j
public class SecurityAspect {
    @Autowired
    private UserRepository repository;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private SecurityService securityService;

    @Pointcut("@within(com.chimera.weapp.annotation.LoginRequired)||" +
            "@annotation(com.chimera.weapp.annotation.LoginRequired)")
    public void onLoginRequired() {
    }

    @Pointcut("@within(com.chimera.weapp.annotation.RolesAllow)||" +
            "@annotation(com.chimera.weapp.annotation.RolesAllow)")
    public void onRolesAllow() {
    }


    // 认证切面，拦截 @LoginRequired 注解的方法
    @Around("onLoginRequired()")
    @Order(1)
    public Object checkLogin(ProceedingJoinPoint pjp) throws Throwable {
        boolean canRefresh = false;
        try {

            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return new ResponseEntity<>("Unauthorized - Token missing or malformed", HttpStatus.UNAUTHORIZED);
            }

            token = token.substring(7);  // 去掉 "Bearer " 前缀
            Claims claims = JwtUtils.parseToken(token);

            String subject = claims.getSubject();
            securityService.compareTokenWithMongoDBToken(token, subject);

            // 存储用户信息，以便后续使用
            ThreadLocalUtil.set(ThreadLocalUtil.USER_DTO, UserDTO.ofUser(repository.findById(new ObjectId(subject)).orElseThrow()).build());
            ThreadLocalUtil.set(ThreadLocalUtil.CLAIMS, claims);
            canRefresh = true;
            return pjp.proceed();

        } catch (ExpiredJwtException | CompareTokenException e) {
            canRefresh = false;
            log.error("token expired", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Throwable e) {
            canRefresh = false;
            throw e;
        } finally {
            if (canRefresh) {
                Claims claims = ThreadLocalUtil.get(ThreadLocalUtil.CLAIMS);

                securityService.tryToRefreshToken(claims);
            }

        }

    }


    // 鉴权切面，拦截 @RoleAllow 注解的方法
    @Around("onRolesAllow()")
    @Order(2)
    public Object checkRole(ProceedingJoinPoint pjp) throws Throwable {
        UserDTO userDTO = ThreadLocalUtil.get(ThreadLocalUtil.USER_DTO);
        if (userDTO == null) {
            return new ResponseEntity<>("Unauthorized - No userDTO found", HttpStatus.UNAUTHORIZED);
        }
        RolesAllow rolesAllow = getRolesAllow(pjp);
        String role = userDTO.getRole();
        boolean hasRequiredRole =
                securityService.hasRequiredRole(role, Arrays.asList(rolesAllow.value()));


        if (!hasRequiredRole) {
            return new ResponseEntity<>(String.format("Forbidden - Insufficient role,your role: %s", role), HttpStatus.FORBIDDEN);
        }
        return pjp.proceed();
    }

    private RolesAllow getRolesAllow(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = pjp.getTarget().getClass();
        RolesAllow rolesAllowFromMethod = method.getAnnotation(RolesAllow.class);
        if (rolesAllowFromMethod != null) {
            return rolesAllowFromMethod;
        } else {
            return targetClass.getAnnotation(RolesAllow.class);
        }
    }

}

