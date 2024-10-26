package com.chimera.weapp.aspect;

import com.chimera.weapp.statemachine.exception.FsmException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class GlobalExceptionAspect {

    // 定义切入点，拦截所有 Controller 层的公共方法
    @Pointcut("execution(* com.chimera.weapp.controller..*(..))")
    public void controllerMethods() {
    }

    // 异常处理切面
    @Around("controllerMethods()")
    public Object handleException(ProceedingJoinPoint pjp) {
        try {
            return pjp.proceed();
        } catch (FsmException e) {
            log.error("状态机出错", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        } catch (Throwable e) {
            log.error("exception on method was not properly caught", e);
            return ResponseEntity
                    .internalServerError()
                    .body("服务器未知错误!");
        }
    }
}

