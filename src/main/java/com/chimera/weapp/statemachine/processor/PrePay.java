package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.DineInContext;
import com.chimera.weapp.statemachine.context.PrePayContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.context.TakeOutContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import com.chimera.weapp.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Processor(processorId = 0)
@Component
public class PrePay extends AbstractStateProcessor<String, PrePayContext> {
    @Autowired
    private CustomRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public boolean filter(StateContext<PrePayContext> context) {
        return true;
    }

    @Override
    public StateEnum getNextState(StateContext<PrePayContext> context) {
        return StateEnum.PAID;
    }

    @Override
    public ServiceResult<String, PrePayContext> actionStep(StateContext<PrePayContext> context) throws Exception {
        User user = userRepository.findById(new ObjectId(context.getUserId())).orElseThrow();
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
        int totalPrice = order.getTotalPrice();
        user.setPoints(user.getPoints() + totalPrice / 100);
        userRepository.save(user);

        ServiceResult<String, PrePayContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public ServiceResult<String, PrePayContext> save(String nextState, StateContext<PrePayContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "", true);
    }

    @Override
    public void after(StateContext<PrePayContext> context) {

    }
}
