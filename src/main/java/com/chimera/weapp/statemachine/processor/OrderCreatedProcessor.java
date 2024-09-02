package com.chimera.weapp.statemachine.processor;


import com.chimera.weapp.statemachine.annotation.processor.OrderProcessor;
import com.chimera.weapp.statemachine.context.InitOrderContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.OrderEventEnum;
import com.chimera.weapp.statemachine.enums.OrderStateEnum;
import com.chimera.weapp.statemachine.enums.ServiceType;
import com.chimera.weapp.statemachine.event.InitEvent;
import com.chimera.weapp.statemachine.pojo.OrderInfo;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@OrderProcessor(state = OrderStateEnum.TO_BE_INIT, bizCode = {"CHEAP", "POPULAR"}, sceneId = "H5", event = OrderEventEnum.INIT)
@Component
public class OrderCreatedProcessor extends AbstractStateProcessor<String, InitOrderContext> {



    @Override
    public ServiceResult<String, InitOrderContext> check(StateContext<InitOrderContext> context) {
        return null;
    }

    @Override
    public ServiceResult<String, InitOrderContext> action(String nextState, StateContext<InitOrderContext> context) throws Exception {
        InitEvent initEvent = (InitEvent) context.getOrderStateEvent();

        // 促销信息信息
        String promtionInfo = this.doPromotion();

        // 订单创建业务处理逻辑...

        ServiceResult<String, InitOrderContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    /**
     * 促销相关扩展点
     */
    protected String doPromotion() {
        return "1";
    }

    @Override
    public OrderStateEnum getNextState(StateContext<InitOrderContext> context) {
        // if (context.getOrderStateEvent().getEventType().equals("xxx")) {
        //     return OrderStateEnum.INIT;
        //  }
        return OrderStateEnum.INITIALIZED;
    }

    @Override
    public ServiceResult<String, InitOrderContext> save(String nextState, StateContext<InitOrderContext> context) throws Exception {
        OrderInfo orderInfo = (OrderInfo) context.getFsmOrder();
        // 更新状态
        orderInfo.setOrderState(nextState);
        // 持久化
//        this.updateOrderInfo(orderInfo);
        log.info("save BUSINESS order success, userId:{}, orderId:{}", orderInfo.getUserId(), orderInfo.getOrderId());
        return new ServiceResult<>(orderInfo.getOrderId(), context.getContext(), "business下单成功", true);
    }

    @Override
    public void after(StateContext<InitOrderContext> context) {

    }

    @Override
    public boolean filter(StateContext<InitOrderContext> context) {
        OrderInfo orderInfo = (OrderInfo) context.getFsmOrder();
        context.setContext(new InitOrderContext<>("estimatePriceInfo"));
        if (orderInfo.getServiceType() == ServiceType.TAKEOFF_CAR) {
            return true;
        }
        return false;
    }
}
