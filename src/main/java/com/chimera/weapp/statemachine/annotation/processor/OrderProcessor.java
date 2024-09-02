package com.chimera.weapp.statemachine.annotation.processor;

import com.chimera.weapp.statemachine.enums.OrderEventEnum;
import com.chimera.weapp.statemachine.enums.OrderStateEnum;
import org.springframework.stereotype.Component;
import java.lang.annotation.*;
/**
 * 状态机引擎的处理器注解标识
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface OrderProcessor {
    int processorId();
}