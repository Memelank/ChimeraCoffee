package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.RefundContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Processor(processorId = 11)
@Component
public class Refund extends AbstractStateProcessor<String, RefundContext> {
    @Autowired
    private CustomRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public boolean filter(StateContext<RefundContext> context) {
        return true;
    }

    @Override
    public StateEnum getNextState(StateContext<RefundContext> context) {
        return StateEnum.REFUNDED;
    }

    @Override
    public ServiceResult<String, RefundContext> action(String nextState, StateContext<RefundContext> context) throws Exception {
        //TODO 呼叫售后跟进
        User user = userRepository.findById(new ObjectId(context.getUserId())).orElseThrow();
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
        int totalPrice = order.getTotalPrice();
        user.setPoints(user.getPoints() - totalPrice / 100);
        userRepository.save(user);

        ServiceResult<String, RefundContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public ServiceResult<String, RefundContext> save(String nextState, StateContext<RefundContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "一段告诉客户售后正在路上的话", true);
    }

    @Override
    public void after(StateContext<RefundContext> context) {

    }
}
