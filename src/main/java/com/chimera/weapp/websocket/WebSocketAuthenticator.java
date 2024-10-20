package com.chimera.weapp.websocket;

public interface WebSocketAuthenticator {
    boolean authenticate(String token); // 认证接口
}
