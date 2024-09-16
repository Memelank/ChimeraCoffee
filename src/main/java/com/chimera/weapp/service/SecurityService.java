package com.chimera.weapp.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {
    @Autowired
    private HttpServletRequest request;

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
}
