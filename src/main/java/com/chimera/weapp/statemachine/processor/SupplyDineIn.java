package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.service.WeChatNoticeService;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.DineInContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.ErrorCodeEnum;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.exception.FsmException;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import lombok.extern.slf4j.Slf4j;
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
        try {
            weChatNoticeService.dineInOrTakeOutNotice(context.getOrderId(), context
                    .getOrderState());
        } catch (Exception e) {
            throw new FsmException(ErrorCodeEnum.SEND_NOTIFICATION_FAILED, e);
        }

    }

}
