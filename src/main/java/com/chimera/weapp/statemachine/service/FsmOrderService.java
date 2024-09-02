package com.chimera.weapp.statemachine.service;

import com.chimera.weapp.statemachine.enums.OrderStateEnum;
import com.chimera.weapp.statemachine.enums.ServiceType;
import com.chimera.weapp.statemachine.pojo.FsmOrder;
import com.chimera.weapp.statemachine.pojo.OrderInfo;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FsmOrderService {

    /**
     * 根据 orderId 查询状态机引擎所需的订单信息基类信息
     */
    public FsmOrder getFsmOrder(String orderId) {
        // TODO 要换成查数据库
        return new OrderInfo(UUID.randomUUID().toString(), OrderStateEnum.TO_BE_INIT.toString(), "POPULAR", "H5", "root", ServiceType.TAKEOFF_CAR);
    }


}