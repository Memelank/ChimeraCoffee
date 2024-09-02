package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.vo.ServiceResult;

public interface StateProcessor<T,C> {
    /**
     * 执行状态迁移的入口
     */
    ServiceResult<T, C> action(StateContext<C> context) throws Exception;
}
