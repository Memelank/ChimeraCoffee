package com.chimera.weapp.controller;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.statemachine.context.InitOrderContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.engine.OrderFsmEngine;
import com.chimera.weapp.statemachine.enums.OrderStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Order createEntity(@RequestBody Order entity) {
        Order rawOrder = new Order();
        rawOrder.setStatus(OrderStateEnum.TO_BE_INIT.toString());
        Order save = repository.save(rawOrder);
        StateContext<InitOrderContext> initOrderContextStateContext = new StateContext<>();
        return repository.save(entity);//TODO 使用状态机创建
    }
}
