package com.chimera.weapp.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.Signature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // 定义切点，这里拦截所有Controller包下的所有类的所有方法
    @Around("execution(* com.chimera.weapp.controller.*.*(..))")
    @Order(0)
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Signature signature = joinPoint.getSignature();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        // 记录方法执行前的信息
        log.info("Before method: {}, with args: {}", methodName, Arrays.toString(args));

        try {
            // 执行原方法
            Object result = joinPoint.proceed();

            // 记录方法执行后的信息
            long endTime = System.currentTimeMillis();
            log.info("After method: {}, execution time: {}ms, result: {}", methodName, endTime - startTime, result);

            return result;
        } catch (Throwable throwable) {
            // 记录异常信息
            log.info("Exception in method: {}, error: {}", methodName, throwable.getMessage());
            throw throwable;
        }
    }
}