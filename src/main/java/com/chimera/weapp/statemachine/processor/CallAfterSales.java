package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.CallAfterSalesContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Processor(processorId = 7)
@Component
public class CallAfterSales extends AbstractStateProcessor<String, CallAfterSalesContext>{
    @Autowired
    private CustomRepository repository;

    @Override
    public boolean filter(StateContext<CallAfterSalesContext> context) {
        return true;
    }

    @Override
    public StateEnum getNextState(StateContext<CallAfterSalesContext> context) {
        return StateEnum.ABNORMAL_END;
    }

    @Override
    public ServiceResult<String, CallAfterSalesContext> action(String nextState, StateContext<CallAfterSalesContext> context) throws Exception {
        //TODO 呼叫售后跟进


        ServiceResult<String, CallAfterSalesContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public ServiceResult<String, CallAfterSalesContext> save(String nextState, StateContext<CallAfterSalesContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "一段告诉客户售后正在路上的话", true);
    }

    @Override
    public void after(StateContext<CallAfterSalesContext> context) {

    }
}
