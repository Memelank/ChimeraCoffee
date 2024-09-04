package com.chimera.weapp.statemachine.engine;

import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.ErrorCodeEnum;
import com.chimera.weapp.statemachine.exception.FsmException;
import com.chimera.weapp.statemachine.processor.AbstractStateProcessor;
import com.chimera.weapp.statemachine.processor.StateProcessor;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * （2）状态机引擎运行时阶段：状态机执行引擎实现
 */
@Component
public class DefaultOrderFsmEngine implements OrderFsmEngine {

    @Autowired
    private CustomRepository customRepository;
    @Autowired
    private DefaultStateProcessRegistry defaultStateProcessRegistry;

    public List<AbstractStateProcessor> acquireStateProcessor(String state, String event, String businessType, String scene) {
        List<Integer> processorIds = customRepository.findProcessorIds(state, event, businessType, scene);
        return processorIds.stream().map(defaultStateProcessRegistry::acquireStateProcessor).toList();
    }


    @Override
    public <T, C> ServiceResult<T, C> sendEvent(String orderEvent, StateContext<C> context) throws Exception {

        // 获取当前事件处理器
        StateProcessor<T, C> stateProcessor = this.getStateProcessor(orderEvent, context);
        // 执行处理逻辑
        return stateProcessor.action(context);//StateProcessor的方法，会调用StateActionStep的一系列方法
    }

    /**
     * 获取当前事件处理器
     */
    private <T, C> StateProcessor<T, C> getStateProcessor(String orderEvent, StateContext<C> context) {
        // 根据 state+event+bizCode+sceneId 信息获取所对应的业务处理器集合
        List<AbstractStateProcessor> processorList = acquireStateProcessor(
                context.getOrderState(), orderEvent,
                context.getBiz(), context.getScene());
        if (processorList == null) {
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
