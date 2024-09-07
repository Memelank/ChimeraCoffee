package com.chimera.weapp.statemachine.annotation.processor;

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
public @interface Processor {
    int processorId();
}