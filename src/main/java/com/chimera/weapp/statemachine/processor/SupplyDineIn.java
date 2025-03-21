package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.config.WebSocketConfig;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.service.WeChatNoticeService;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.DineInContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.ErrorCodeEnum;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.exception.FsmException;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Processor(processorId = 3)
@Component
public class SupplyDineIn extends AbstractStateProcessor<String, DineInContext> {
    @Autowired
    private CustomRepository repository;
    @Autowired
    private WeChatNoticeService weChatNoticeService;
    @Autowired
    private WebSocketConfig webSocketConfig;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean filter(StateContext<DineInContext> context) {
        return true;
    }

    @Override
    public StateEnum getNextState(StateContext<DineInContext> context) {
        return StateEnum.NORMAL_END;
    }

    @Override
    public ServiceResult<String, DineInContext> actionStep(StateContext<DineInContext> context) throws Exception {

        ServiceResult<String, DineInContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public ServiceResult<String, DineInContext> save(String nextState, StateContext<DineInContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "一段告诉店员订单正常结束的话", true);
    }

    @Override
    public void after(StateContext<DineInContext> context) {
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
        try {
            webSocketConfig.getOrderUpdateWebSocketHandler().sendOrderJson(context.getOrderId(), objectMapper.writeValueAsString(order));
        } catch (JsonProcessingException e) {
            log.error("序列化order时竟然出错！", e);
            throw new RuntimeException(e);
        }

    }

}
