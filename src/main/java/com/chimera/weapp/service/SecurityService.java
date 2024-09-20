package com.chimera.weapp.service;

import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SecurityService {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private UserRepository userRepository;

    public void checkIdImitate(String idFromParameterOrBody) {
        Claims claims = (Claims) request.getAttribute("claims");
        String userId = (String) claims.get("userId");
        if (!userId.equals(idFromParameterOrBody)) {
            throw new RuntimeException("禁止冒充他人身份进行操作");
        }
    }

    public void checkIdImitate(ObjectId idFromParameterOrBody) {
        checkIdImitate(idFromParameterOrBody.toHexString());
    }

    public void tryToRefreshToken(Claims claims) {
        Date now = new Date();
        Date expiration = claims.getExpiration();
        // 如果令牌即将过期，刷新令牌
        if (expiration.getTime() - now.getTime() < 24 * 60 * 60 * 1000) {  // 如果距离过期少于一天
            String userId = (String) claims.get("userId");
            String userName = (String) claims.get("userName");
            String role = (String) claims.get("role");
            String newToken = JwtUtils.generateToken(userId, userName, role, null);

            response.setHeader("Authorization", "Bearer " + newToken);

            User user = userRepository.findById(new ObjectId(userId))
                    .orElseThrow(() -> new RuntimeException("user did not found when refresh"));
            user.setJwt(newToken);
            userRepository.save(user);
        }
    }
}
