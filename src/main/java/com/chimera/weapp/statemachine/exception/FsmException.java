package com.chimera.weapp.statemachine.exception;

import com.chimera.weapp.statemachine.enums.ErrorCodeEnum;

public class FsmException extends RuntimeException {
    public FsmException(String step, FsmException e) {
        super(String.format("状态机失败在[%s],具体信息:%s", step, e.getMessage()), e);
    }

    public FsmException(ErrorCodeEnum errCode) {
        super(errCode.toString());
    }

    public FsmException(ErrorCodeEnum errCode, String msg) {
        super(errCode.toString() + msg);
    }

    public FsmException(ErrorCodeEnum errCode, Exception e) {
        super(errCode.toString(), e);
    }

    public FsmException(String step) {
        super(String.format("状态机失败在[%s]", step));
    }
}
