package com.chimera.weapp.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class OrderCreateWebSocketHandler extends AuthenticatedWebSocketHandler {
    private final List<WebSocketSession> sessions = new ArrayList<>();

    public OrderCreateWebSocketHandler(WebSocketAuthenticator authenticator, ScheduledExecutorService scheduler, long authTimeoutSeconds) {
        super(authenticator, scheduler, authTimeoutSeconds);
    }



    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 移除关闭连接的订单ID
        sessions.remove(session);
    }

    public void sendMessage(String message) throws IOException {
        for (WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
