package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.TakeOutContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Processor(processorId = 9)
@Component
public class SupplyTakeOut extends AbstractStateProcessor<String, TakeOutContext> {
    @Autowired
    private CustomRepository repository;

    @Override
    public boolean filter(StateContext<TakeOutContext> context) {
        return true;
    }

    @Override
    public StateEnum getNextState(StateContext<TakeOutContext> context) {
        return StateEnum.NORMAL_END;
    }

    @Override
    public ServiceResult<String, TakeOutContext> actionStep(StateContext<TakeOutContext> context) throws Exception {

        ServiceResult<String, TakeOutContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public ServiceResult<String, TakeOutContext> save(String nextState, StateContext<TakeOutContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "一段告诉店员订单正常结束的话", true);
    }

    @Override
    public void after(StateContext<TakeOutContext> context) {
        //todo 提醒顾客可以取餐了
    }
}
