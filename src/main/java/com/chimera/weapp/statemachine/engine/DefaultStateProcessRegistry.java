package com.chimera.weapp.statemachine.engine;


import com.chimera.weapp.repository.ProcessorRepository;
import com.chimera.weapp.statemachine.annotation.processor.OrderProcessor;
import com.chimera.weapp.statemachine.processor.AbstractStateProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DefaultStateProcessRegistry implements BeanPostProcessor {
    private static final Map<Integer, AbstractStateProcessor> stateProcessMap = new ConcurrentHashMap<>();
    @Autowired
    private ProcessorRepository processorRepository;

    /**
     * 获取bean注解，对状态机引擎的处理器注解 {@link OrderProcessor} 进行解析处理
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AbstractStateProcessor && bean.getClass().isAnnotationPresent(OrderProcessor.class)) {
            OrderProcessor annotation = bean.getClass().getAnnotation(OrderProcessor.class);
            int i = annotation.processorId();
            stateProcessMap.put(i, (AbstractStateProcessor) bean);
        }
        return bean;
    }

    public AbstractStateProcessor acquireStateProcess(int processorId) {
        return stateProcessMap.get(processorId);
    }

    public List<AbstractStateProcessor> acquireStateProcess(String state, String event, String businessType, String scene) {
        int[] processorIds = processorRepository.findProcessorIds(state, event, businessType, scene);
        return Arrays.stream(processorIds).mapToObj(stateProcessMap::get).toList();
    }

}
