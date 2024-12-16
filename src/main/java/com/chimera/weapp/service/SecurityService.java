package com.chimera.weapp.service;

import com.chimera.weapp.dto.UserDTO;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.exception.CompareTokenException;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.util.JwtUtils;
import com.chimera.weapp.util.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.chimera.weapp.util.JwtUtils.REFRESH_TIME;

@Component
public class SecurityService {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private UserRepository userRepository;

    public void checkIdImitate(String idFromParameterOrBody) {
        UserDTO userDTO = ThreadLocalUtil.get(ThreadLocalUtil.USER_DTO);
        if (!Objects.equals(userDTO.getId(), idFromParameterOrBody)) {
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
        if (expiration.getTime() - now.getTime() < REFRESH_TIME) {  // 如果距离过期少于一天
            String userId = claims.getSubject();
            String newToken = JwtUtils.generateToken(userId);

            response.setHeader("Authorization", "Bearer " + newToken);

            User user = userRepository.findById(new ObjectId(userId))
                    .orElseThrow(() -> new RuntimeException("user did not found when refresh"));
            user.setJwt(newToken);
            userRepository.save(user);
        }
    }

    public void compareTokenWithMongoDBToken(String token, String userId) {
        User user = userRepository.findById(new ObjectId(userId)).orElseThrow(() -> new RuntimeException("user not found"));
        String jwt = user.getJwt();
        if (jwt == null || jwt.isEmpty()) {
            throw new CompareTokenException("Unauthorized - User had log out");
        }
        if (!token.equals(jwt)) {
            throw new CompareTokenException("Unauthorized - Token is different");
        }
    }

    /**
     * @param role 输入角色
     */
    public boolean hasRequiredRole(String role, List<RoleEnum> roleEnumList) {
        return roleEnumList.stream().anyMatch(roleEnum -> Objects.equals(role, roleEnum.toString()));
    }

    /**
     * @param userId 输入用户id
     */
    public boolean hasRequiredRole(ObjectId userId, List<RoleEnum> roleEnumList) {
        User user = userRepository.findById(userId).orElseThrow();
        String role = user.getRole();
        return hasRequiredRole(role, roleEnumList);
    }
}
