package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;

public interface StateActionStep<T, C> {
    /**
     * 准备数据
     */
    default void prepare(StateContext<C> context) {
    }

    /**
     * 校验
     */
    ServiceResult<T, C> check(StateContext<C> context);

    /**
     * 状态动作方法，主要状态迁移逻辑
     */
    ServiceResult<T, C> actionStep(StateContext<C> context) throws Exception;

    /**
     * 获取当前状态处理器处理完毕后，所处于的下一个状态，这里把下一个状态的判定交由业务逻辑根据上下文对象自己来判断。
     */
    StateEnum getNextState(StateContext<C> context);

    /**
     * 状态数据持久化
     */
    ServiceResult<T, C> save(String nextState, StateContext<C> context) throws Exception;

    /**
     * 状态迁移成功，持久化后执行的后续处理
     */
    void after(StateContext<C> context);
}
