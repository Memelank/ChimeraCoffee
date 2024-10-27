package com.chimera.weapp.statemachine.context;

import com.chimera.weapp.entity.Order;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class StateContext<C> {
    /**
     * 状态机需要的订单基本信息
     */
    private String userId;

    private String orderId;

    private String orderState;

    private String customerType;

    private String scene;
    /**
     * 业务可定义的上下文泛型对象
     */
    private C context;

    public StateContext(Order order, C c) {
        this.userId = order.getUserId().toHexString();
        this.orderId = order.getId().toHexString();
        this.orderState = order.getState();
        this.customerType = order.getCustomerType();
        this.scene = order.getScene();
        this.context = c;
    }
}