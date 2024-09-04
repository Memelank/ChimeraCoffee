package com.chimera.weapp.controller;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.statemachine.context.InitOrderContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.engine.OrderFsmEngine;
import com.chimera.weapp.statemachine.enums.OrderEventEnum;
import com.chimera.weapp.statemachine.enums.OrderStateEnum;
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
        entity.setState(OrderStateEnum.TO_BE_INIT.toString());
        Order save = repository.save(entity);
        StateContext<InitOrderContext> context = new StateContext<>();
        setContext(context, save);
        ServiceResult<Object, InitOrderContext> serviceResult =
                orderFsmEngine.sendEvent(OrderEventEnum.INIT.toString(), context);
        if (serviceResult.isSuccess()) {
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }
    }

    private void setContext(StateContext<InitOrderContext> context, Order save) {
        context.setOrderId(save.getId().toString());
        context.setUserId(save.getUserId().toString());
        context.setOrderState(save.getState());
        context.setBiz(save.getBiz());
        context.setScene(save.getScene());
    }
}
