package com.chimera.weapp.statemachine.engine;

import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.ErrorCodeEnum;
import com.chimera.weapp.statemachine.enums.OrderEventEnum;
import com.chimera.weapp.statemachine.event.OrderStateEvent;
import com.chimera.weapp.statemachine.exception.FsmException;
import com.chimera.weapp.statemachine.pojo.FsmOrder;
import com.chimera.weapp.statemachine.processor.AbstractStateProcessor;
import com.chimera.weapp.statemachine.processor.StateProcessor;
import com.chimera.weapp.statemachine.service.FsmOrderService;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * （2）状态机引擎运行时阶段：状态机执行引擎实现
 */
@Component
public class DefaultOrderFsmEngine implements OrderFsmEngine {

    @Autowired
    private FsmOrderService fsmOrderService;
    @Autowired
    private CustomRepository customRepository;
    @Autowired
    private DefaultStateProcessRegistry defaultStateProcessRegistry;

    public List<AbstractStateProcessor> acquireStateProcessor(String state, String event, String businessType, String scene) {
        List<Integer> processorIds = customRepository.findProcessorIds(state, event, businessType, scene);
        return processorIds.stream().map(defaultStateProcessRegistry::acquireStateProcessor).toList();
    }

    @Override
    public <T, C> ServiceResult<T, C> sendEvent(OrderStateEvent orderStateEvent, StateContext<C> context) throws Exception {
        FsmOrder fsmOrder = null;
        if (OrderEventEnum.INIT.toString().equals(orderStateEvent.getEventType())) {
            fsmOrder = fsmOrderService.getFsmOrder(orderStateEvent.getOrderId());
            if (fsmOrder == null) {
                throw new FsmException(ErrorCodeEnum.ORDER_NOT_FOUND);
            }
        }
        return sendEvent(orderStateEvent, fsmOrder, context);
    }

    @Override
    public <T, C> ServiceResult<T, C> sendEvent(OrderStateEvent orderStateEvent, FsmOrder fsmOrder, StateContext<C> context) throws Exception {

        // 获取当前事件处理器
        StateProcessor<T, C> stateProcessor = this.getStateProcessor(context);
        // 执行处理逻辑
        return stateProcessor.action(context);//StateProcessor的方法，会调用StateActionStep的一系列方法
    }

    /**
     * 获取当前事件处理器
     */
    private <T, C> StateProcessor<T, C> getStateProcessor(StateContext<C> context) {
        OrderStateEvent stateEvent = context.getOrderStateEvent();
        FsmOrder fsmOrder = context.getFsmOrder();
        // 根据 state+event+bizCode+sceneId 信息获取所对应的业务处理器集合
        List<AbstractStateProcessor> processorList = acquireStateProcessor(
                fsmOrder.getOrderState(), stateEvent.getEventType(),
                fsmOrder.bizCode(), fsmOrder.sceneId());
        if (processorList == null) {
            // 订单状态发生改变
            if (!Objects.isNull(stateEvent.orderState()) &&
                    !stateEvent.orderState().equals(fsmOrder.getOrderState())) {
                throw new FsmException(ErrorCodeEnum.ORDER_STATE_NOT_MATCH);
            }
            throw new FsmException(ErrorCodeEnum.NOT_FOUND_PROCESSOR);
        }
        List<AbstractStateProcessor> processorResult = new ArrayList<>(processorList.size());
        // 根据上下文获取唯一的业务处理器
        // 因为有可能根据 state+event+bizCode+sceneId 信息获取到的是多个状态处理器processor，有可能确实业务需要单纯依赖bizCode和sceneId两个属性无法有效识别和定位唯一processor，那么我们这里给业务开一个口、由业务决定从多个处理器中选一个适合当前上下文的，具体做法是业务processor通过filter方法根据当前context来判断是否符合调用条件。
        for (AbstractStateProcessor processor : processorList) {
            if (processor.filter(context)) {
                processorResult.add(processor);
            }
        }
        //如果最终经过业务filter之后，还是有多个状态处理器符合条件，那么这里只能抛异常处理了。这个需要在开发时，对状态和多维度处理器有详细规划。
        if (CollectionUtils.isEmpty(processorResult)) {
            throw new FsmException(ErrorCodeEnum.NOT_FOUND_PROCESSOR);
        }
        if (processorResult.size() > 1) {
            throw new FsmException(ErrorCodeEnum.FOUND_MORE_PROCESSOR);
        }
        return processorResult.get(0);
    }

}
