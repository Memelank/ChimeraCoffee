package com.chimera.weapp.controller;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.statemachine.context.DineInContext;
import com.chimera.weapp.statemachine.context.FixDeliveryContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.engine.OrderFsmEngine;

import com.chimera.weapp.statemachine.enums.EventEnum;
import com.chimera.weapp.statemachine.enums.SceneEnum;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderRepository repository;

    @Autowired
    private OrderFsmEngine orderFsmEngine;

    @GetMapping
    public List<Order> getAllEntities() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<ServiceResult> createOrder(@RequestBody Order entity) throws Exception {
        //TODO 微信支付流程?可能在这
        entity.setState(StateEnum.PAID.toString());
        Order save = repository.save(entity);
        ServiceResult<Object, ?> serviceResult = null;

        StateContext<Object> context = new StateContext<>();
        setContext(context, save);
        if (SceneEnum.FIX_DELIVERY.toString().equals(save.getScene())) {
            FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
            context.setContext(fixDeliveryContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_FIX_DELIVERY.toString(), context);
        } else if (SceneEnum.DINE_IN.toString().equals(save.getScene())) {
            DineInContext dineInContext = new DineInContext();
            context.setContext(dineInContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_DINE_IN.toString(), context);
        }

        if (serviceResult != null && serviceResult.isSuccess()) {
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }
    }

    private void setContext(StateContext<?> context, Order save) {
        context.setOrderId(save.getId().toString());
        context.setUserId(save.getUserId().toString());
        context.setOrderState(save.getState());
        context.setCustomerType(save.getCustomerType());
        context.setScene(save.getScene());
    }
}
