package com.chimera.weapp.statemachine.pojo;

import com.chimera.weapp.statemachine.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo implements FsmOrder {
    private String orderId;
    private String orderState;
    private String bizCode;
    private String sceneId;
    private String userId;
    private ServiceType serviceType;

    @Override
    public String bizCode() {
        return bizCode;
    }

    @Override
    public String sceneId() {
        return sceneId;
    }
}