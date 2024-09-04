package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.statemachine.annotation.processor.OrderProcessor;
import com.chimera.weapp.statemachine.context.InitOrderContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.OrderStateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@OrderProcessor(processorId = 1)
@Component
public class OrderInitProcessor extends AbstractStateProcessor<String, InitOrderContext> {
    @Autowired
    private CustomRepository repository;

    @Override
    public boolean filter(StateContext<InitOrderContext> context) {
        return true;
    }

    @Override
    public OrderStateEnum getNextState(StateContext<InitOrderContext> context) {
        return OrderStateEnum.INITIALIZED;
    }

    @Override
    public ServiceResult<String, InitOrderContext> action(String nextState, StateContext<InitOrderContext> context) throws Exception {
        // 订单创建业务处理逻辑...

        ServiceResult<String, InitOrderContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public ServiceResult<String, InitOrderContext> save(String nextState, StateContext<InitOrderContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "business下单成功", true);
    }

    @Override
    public void after(StateContext<InitOrderContext> context) {

    }

}
