package com.chimera.weapp.statemachine.event;

import com.chimera.weapp.statemachine.enums.OrderEventEnum;

import java.util.UUID;

public class InitEvent implements OrderStateEvent{
    @Override
    public String getEventType() {
        return OrderEventEnum.INIT.toString();
    }

    @Override
    public String getOrderId() {//TODO 应改成查数据库
        return UUID.randomUUID().toString();
    }

}
