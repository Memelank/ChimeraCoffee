package com.chimera.weapp.statemachine.engine;

import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.vo.ServiceResult;

/**
 * （2）状态机引擎运行时阶段：状态机执行引擎接口
 */
public interface OrderFsmEngine {
    <T, C> ServiceResult<T, C> sendEvent(String orderEvent, StateContext<C> context) throws Exception;
}