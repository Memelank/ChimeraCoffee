package com.chimera.weapp.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BaseWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketAuthenticator authenticator;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<WebSocketSession, Long> sessionHeartbeatMap = new ConcurrentHashMap<>();
    private final long authTimeoutSeconds; // 认证鉴权超时时间
    private static final String AUTHENTICATED_ATTR = "authenticated";
    private final long heartBeatTimeout; // 心跳超时时间


    // 通过构造函数注入认证器和其他配置
    public BaseWebSocketHandler(WebSocketAuthenticator authenticator, ScheduledExecutorService scheduler, long authTimeoutSeconds, long heartBeatTimeout) {
        this.authenticator = authenticator;
        this.scheduler = scheduler;
        this.authTimeoutSeconds = authTimeoutSeconds;
        this.heartBeatTimeout = heartBeatTimeout;
        scheduler.scheduleAtFixedRate(this::checkHeartbeats, 0, 5, TimeUnit.SECONDS);

    }

    // 定期检查心跳，断开超时的连接
    private void checkHeartbeats() {
        long now = System.currentTimeMillis();
        sessionHeartbeatMap.forEach((session, lastHeartbeat) -> {
            if (now - lastHeartbeat > heartBeatTimeout * 1000) {
                // 超时，关闭连接
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage("heartbeat timeout!"));
                        session.close();
                        log.info("Closed session due to timeout: {}", session.getId());
                    }
                } catch (Exception e) {
                    log.error("在断开在因心跳超时的连接时出错", e);
                } finally {
                    sessionHeartbeatMap.remove(session);
                }
            }
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established.");
        sessionHeartbeatMap.put(session, System.currentTimeMillis());

        // 对认证设置超时机制
        scheduler.schedule(() -> {
            Boolean authenticated = (Boolean) session.getAttributes().get(AUTHENTICATED_ATTR);
            if (authenticated == null || !authenticated) {
                try {
                    log.info("Authentication timed out. Closing session.");
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage("authentication timeout!"));
                        session.close(CloseStatus.NOT_ACCEPTABLE); // 超时未认证，关闭连接
                    }
                } catch (Exception e) {
                    log.warn("关闭连接异常", e);
                }
            }
        }, authTimeoutSeconds, TimeUnit.SECONDS); // 配置化超时时间
    }

    /**
     * 既有认证鉴权分支，又有心跳分支
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if ("ping".equals(payload)) {
            // 收到心跳消息，更新最后收到消息的时间
            sessionHeartbeatMap.put(session, System.currentTimeMillis());
            // 回复心跳
            session.sendMessage(new TextMessage("pong"));
        } else if (payload.contains("authenticate")) {// 解析消息，假设消息类型为 "authenticate" 并包含 token
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
        log.info("WebSocket connection closed.");
    }

}
