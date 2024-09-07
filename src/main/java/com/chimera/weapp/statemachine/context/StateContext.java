package com.chimera.weapp.statemachine.context;

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
}