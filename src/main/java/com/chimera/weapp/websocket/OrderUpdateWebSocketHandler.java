package com.chimera.weapp.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class OrderUpdateWebSocketHandler extends BaseWebSocketHandler {

    // 存储orderId和WebSocketSession的映射
    private final Map<String, WebSocketSession> orderSessionMap = new ConcurrentHashMap<>();

    public OrderUpdateWebSocketHandler(WebSocketAuthenticator authenticator, ScheduledExecutorService scheduler, long authTimeoutSeconds, int heartBeatTimeout) {
        super(authenticator, scheduler, authTimeoutSeconds, heartBeatTimeout);
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        URI uri = session.getUri();  // 获取 WebSocket 的 URI

        // 使用 UriComponentsBuilder 来解析查询参数
        UriComponents uriComponents = UriComponentsBuilder.fromUri(uri).build();
        String orderId = uriComponents.getQueryParams().getFirst("orderId");

        if (orderId != null) {
            orderSessionMap.put(orderId, session);  // 记录 orderId 和 WebSocketSession 的映射
            log.info("连接成功, orderId: {}", orderId);
        } else {
            log.info("未传递 orderId 参数");
            session.close();  // 如果没有传递 orderId，则关闭连接
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        // 移除关闭连接的订单ID
        orderSessionMap.values().remove(session);
    }

    // 发送订单状态更新消息给特定orderId的客户端
    public void sendMessageToOrder(String orderId, String message) {
        WebSocketSession session = orderSessionMap.get(orderId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("OrderWebSocketHandler send msg error", e);
            }
        } else {
            log.warn("order {} 的webSocket不存在或已关闭", orderId);
        }
    }

    public void sendOrderJson(String orderId, String orderJson) {
        sendMessageToOrder(orderId,"order_json"+orderJson);
    }
}


