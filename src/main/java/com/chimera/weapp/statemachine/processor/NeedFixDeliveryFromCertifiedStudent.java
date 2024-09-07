package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.DineInContext;
import com.chimera.weapp.statemachine.context.FixDeliveryContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Processor(processorId = 2)
@Component
public class NeedFixDeliveryFromCertifiedStudent extends AbstractStateProcessor<String, FixDeliveryContext> {
    @Autowired
    private CustomRepository repository;

    @Override
    public boolean filter(StateContext<FixDeliveryContext> context) {
        return true;
    }

    @Override
    public StateEnum getNextState(StateContext<FixDeliveryContext> context) {
        return StateEnum.WAITING_FIX_DELIVERY;
    }

    @Override
    public ServiceResult<String, FixDeliveryContext> action(String nextState, StateContext<FixDeliveryContext> context) throws Exception {
        //TODO 提醒店员准备派送

        ServiceResult<String, FixDeliveryContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public ServiceResult<String, FixDeliveryContext> save(String nextState, StateContext<FixDeliveryContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "一段告诉客户店员会准备派送的话", true);
    }

    @Override
    public void after(StateContext<FixDeliveryContext> context) {

    }
}
