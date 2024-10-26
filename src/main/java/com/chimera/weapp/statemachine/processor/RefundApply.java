package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.service.WeChatRequestService;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.RefundApplyContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import com.wechat.pay.java.service.refund.model.Refund;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Processor(processorId = 12)
@Component
public class RefundApply extends AbstractStateProcessor<String, RefundApplyContext>{
    @Autowired
    private CustomRepository repository;
    @Autowired
    private WeChatRequestService weChatRequestService;
    @Autowired
    private OrderRepository orderRepository;
    @Override
    public boolean filter(StateContext<RefundApplyContext> context) {
        return false;
    }

    @Override
    public ServiceResult<String, RefundApplyContext> actionStep(StateContext<RefundApplyContext> context) throws Exception {
        //TODO 调用微信的退款接口&呼叫售后跟进
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
        Refund refund = weChatRequestService.createRefund(order, context.getContext().getReason());
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
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "", true);
    }

    @Override
    public void after(StateContext<RefundApplyContext> context) {

    }
}
