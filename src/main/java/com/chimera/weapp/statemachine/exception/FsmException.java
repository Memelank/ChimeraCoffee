package com.chimera.weapp.statemachine.exception;

import com.chimera.weapp.statemachine.enums.ErrorCodeEnum;

public class FsmException extends RuntimeException {
    public FsmException(ErrorCodeEnum errCode) {
    }
}
