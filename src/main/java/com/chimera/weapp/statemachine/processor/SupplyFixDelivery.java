package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.service.WeChatNoticeService;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.FixDeliveryContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import com.chimera.weapp.vo.DeliveryInfo;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Processor(processorId = 4)
@Component
public class SupplyFixDelivery extends AbstractStateProcessor<String, FixDeliveryContext> {
    @Autowired
    private CustomRepository repository;
    @Autowired
    private WeChatNoticeService weChatNoticeService;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public boolean filter(StateContext<FixDeliveryContext> context) {
        return true;
    }

    @Override
    public StateEnum getNextState(StateContext<FixDeliveryContext> context) {
        return StateEnum.NORMAL_END;
    }

    @Override
    public ServiceResult<String, FixDeliveryContext> actionStep(StateContext<FixDeliveryContext> context) throws Exception {

        ServiceResult<String, FixDeliveryContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public ServiceResult<String, FixDeliveryContext> save(String nextState, StateContext<FixDeliveryContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "一段告诉派送员订单正常结束的话", true);
    }

    @Override
    public void after(StateContext<FixDeliveryContext> context) {
        FixDeliveryContext fixDeliveryContext = context.getContext();
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        weChatNoticeService.fixDeliveryNotice(context.getOrderId(), deliveryInfo.getTime().toString(), deliveryInfo.getAddress());
    }
}
