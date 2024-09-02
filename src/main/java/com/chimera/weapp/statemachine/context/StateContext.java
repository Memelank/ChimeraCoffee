package com.chimera.weapp.statemachine.context;

import com.chimera.weapp.statemachine.event.OrderStateEvent;
import com.chimera.weapp.statemachine.pojo.FsmOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class StateContext<C> {
    /**
     * 订单操作事件
     */
    private OrderStateEvent orderStateEvent;
    /**
     * 状态机需要的订单基本信息
     */
    private FsmOrder fsmOrder;
    /**
     * 业务可定义的上下文泛型对象
     */
    private C context;

    public StateContext(OrderStateEvent orderStateEvent, FsmOrder fsmOrder) {
        this.orderStateEvent = orderStateEvent;
        this.fsmOrder = fsmOrder;
    }
}