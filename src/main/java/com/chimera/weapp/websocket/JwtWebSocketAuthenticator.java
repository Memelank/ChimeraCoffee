package com.chimera.weapp.websocket;

import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.service.SecurityService;
import com.chimera.weapp.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.util.List;


@AllArgsConstructor
@Slf4j
public class JwtWebSocketAuthenticator implements WebSocketAuthenticator {

    private List<RoleEnum> roleEnums;

    private SecurityService securityService;

    @Override
    public boolean authenticate(String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            String subject = claims.getSubject();
            return securityService.hasRequiredRole(new ObjectId(subject), roleEnums);
        } catch (Exception e) {
            log.warn("jwt认证失败", e);
            return false;
        }
    }

}
