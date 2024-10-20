package com.chimera.weapp.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AuthenticatedWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketAuthenticator authenticator;
    private final ScheduledExecutorService scheduler;
    private final long authTimeoutSeconds;
    private static final String AUTHENTICATED_ATTR = "authenticated";

    // 通过构造函数注入认证器和其他配置
    public AuthenticatedWebSocketHandler(WebSocketAuthenticator authenticator, ScheduledExecutorService scheduler, long authTimeoutSeconds) {
        this.authenticator = authenticator;
        this.scheduler = scheduler;
        this.authTimeoutSeconds = authTimeoutSeconds;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established.");

        // 设置超时机制
        scheduler.schedule(() -> {
            Boolean authenticated = (Boolean) session.getAttributes().get(AUTHENTICATED_ATTR);
            if (authenticated == null || !authenticated) {
                try {
                    log.info("Authentication timed out. Closing session.");
                    session.close(CloseStatus.NOT_ACCEPTABLE); // 超时未认证，关闭连接
                } catch (Exception e) {
                    log.warn("关闭连接异常", e);
                }
            }
        }, authTimeoutSeconds, TimeUnit.SECONDS); // 配置化超时时间
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // 解析消息，假设消息类型为 "authenticate" 并包含 token
        if (payload.contains("authenticate")) {
            String authorization = extractTokenFromMessage(payload); // 自行实现 authorization 提取逻辑

            if (authenticator.authenticate(authorization.substring(7))) {
                session.getAttributes().put(AUTHENTICATED_ATTR, true); // 将认证状态存储在 session 中
                session.sendMessage(new TextMessage("authenticate:successful!"));
            } else {
                session.sendMessage(new TextMessage("authenticate:failed!"));
                session.close(CloseStatus.NOT_ACCEPTABLE); // 认证失败，关闭连接
            }
        } else {
            // 如果未认证就发送其他消息，直接关闭连接
            Boolean authenticated = (Boolean) session.getAttributes().get(AUTHENTICATED_ATTR);
            if (authenticated == null || !authenticated) {
                session.sendMessage(new TextMessage("Unauthorized!"));
                session.close(CloseStatus.NOT_ACCEPTABLE);
            }
        }
    }

    private String extractTokenFromMessage(String message) {
        // 提取 token 的逻辑
        return message.split(":")[1]; // 这是简化示例，根据实际情况进行解析
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("WebSocket connection closed.");
    }

}
