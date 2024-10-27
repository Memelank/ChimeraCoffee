package com.chimera.weapp.config;

import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.service.SecurityService;
import com.chimera.weapp.websocket.JwtWebSocketAuthenticator;
import com.chimera.weapp.websocket.OrderUpdateWebSocketHandler;
import com.chimera.weapp.websocket.OrderCreateOrEndWebSocketHandler;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {


    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Getter
    private OrderCreateOrEndWebSocketHandler orderCreateWebSocketHandler;

    @Getter
    private OrderCreateOrEndWebSocketHandler orderEndWebSocketHandler;

    @Getter
    private OrderUpdateWebSocketHandler orderUpdateWebSocketHandler;

    @Autowired
    private SecurityService securityService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        orderUpdateWebSocketHandler = new OrderUpdateWebSocketHandler(
                new JwtWebSocketAuthenticator(Arrays.asList(RoleEnum.values()), securityService),
                scheduler,
                10, 30);
        registry.addHandler(orderUpdateWebSocketHandler, "/ws/order_update")
                .setAllowedOrigins("*"); // 允许跨域
        orderCreateWebSocketHandler =
                new OrderCreateOrEndWebSocketHandler(
                        new JwtWebSocketAuthenticator(List.of(RoleEnum.ADMIN), securityService),
                        scheduler,
                        10, 30);
        registry.addHandler(orderCreateWebSocketHandler, "/ws/order_create")
                .setAllowedOrigins("*");
        orderEndWebSocketHandler =
                new OrderCreateOrEndWebSocketHandler(
                        new JwtWebSocketAuthenticator(List.of(RoleEnum.ADMIN), securityService),
                        scheduler,
                        10, 30);
        registry.addHandler(orderEndWebSocketHandler, "/ws/order_end")
                .setAllowedOrigins("*");

    }
}
