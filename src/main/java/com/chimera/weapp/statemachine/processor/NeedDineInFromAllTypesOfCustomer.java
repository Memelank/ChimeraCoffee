package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.DineInContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Processor(processorId = 1)
@Component
public class NeedDineInFromAllTypesOfCustomer extends AbstractStateProcessor<String, DineInContext> {
    @Autowired
    private CustomRepository repository;

    @Override
    public boolean filter(StateContext<DineInContext> context) {
        return true;
    }

    @Override
    public StateEnum getNextState(StateContext<DineInContext> context) {
        return StateEnum.WAITING_DINE_IN;
    }

    @Override
    public ServiceResult<String, DineInContext> action(String nextState, StateContext<DineInContext> context) throws Exception {
        //TODO 提醒店员做单了

        ServiceResult<String, DineInContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public ServiceResult<String, DineInContext> save(String nextState, StateContext<DineInContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "一段告诉客户店员正在做单的话", true);
    }

    @Override
    public void after(StateContext<DineInContext> context) {

    }

}