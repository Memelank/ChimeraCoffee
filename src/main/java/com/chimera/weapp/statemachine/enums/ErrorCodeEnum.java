package com.chimera.weapp.statemachine.enums;

public enum ErrorCodeEnum {
    ORDER_NOT_FOUND("订单不存在"),
    ORDER_STATE_NOT_MATCH("订单状态不匹配"),
    NOT_FOUND_PROCESSOR("未找到处理器"),
    FOUND_MORE_PROCESSOR("处理器数量超过上限"),
    ;

    ErrorCodeEnum(String errMsg) {
    }
}
