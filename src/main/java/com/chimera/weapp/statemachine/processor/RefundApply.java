package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.service.WeChatRequestService;
import com.chimera.weapp.service.WechatPaymentService;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.RefundApplyContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Processor(processorId = 10)
@Component
public class RefundApply extends AbstractStateProcessor<String, RefundApplyContext> {
    @Autowired
    private CustomRepository repository;
    @Autowired
    private WechatPaymentService wechatPaymentService;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public boolean filter(StateContext<RefundApplyContext> context) {
        return true;
    }

    @Override
    public ServiceResult<String, RefundApplyContext> actionStep(StateContext<RefundApplyContext> context) throws Exception {
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
        wechatPaymentService.createRefund(order, context.getContext().getReason());//todo 或许可以拿着返回值干点别的事
        ServiceResult<String, RefundApplyContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public StateEnum getNextState(StateContext<RefundApplyContext> context) {
        return StateEnum.WAITING_REFUND_NOTIFICATION;
    }

    @Override
    public ServiceResult<String, RefundApplyContext> save(String nextState, StateContext<RefundApplyContext> context) throws Exception {
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
        order.setState(nextState);
        order.setRefundReason(context.getContext().getReason());
        orderRepository.save(order);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "", true);
    }

    @Override
    public void after(StateContext<RefundApplyContext> context) {

    }
}
