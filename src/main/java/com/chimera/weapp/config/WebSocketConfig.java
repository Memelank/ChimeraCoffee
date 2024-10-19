package com.chimera.weapp.config;

import com.chimera.weapp.handler.OrderUpdateWebSocketHandler;
import com.chimera.weapp.handler.OrderCreateWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private OrderUpdateWebSocketHandler orderUpdateWebSocketHandler;

    @Autowired
    private OrderCreateWebSocketHandler orderCreateWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderUpdateWebSocketHandler, "/ws/order_update")
                .addHandler(orderCreateWebSocketHandler,"/ws/order_create")
                .setAllowedOrigins("*"); // 允许跨域
    }
}
