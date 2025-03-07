package com.chimera.weapp.statemachine.enums;

public enum ErrorCodeEnum {
    ORDER_NOT_FOUND("订单不存在"),
    ORDER_STATE_NOT_MATCH("订单状态不匹配"),
    NOT_FOUND_PROCESSOR("状态机根据条件未找到处理器"),
    FILTER_NOT_FOUND_PROCESSOR("状态机根据条件未找到处理器（过滤后）"),
    FOUND_MORE_PROCESSOR("状态机根据条件找到的处理器数量超过上限"),
    SEND_NOTIFICATION_FAILED("发送通知失败");
    private final String errMsg;

    // 构造函数接收自定义错误消息字符串
    ErrorCodeEnum(String errMsg) {
        this.errMsg = errMsg;
    }

    // 重写toString方法，返回自定义错误消息字符串
    @Override
    public String toString() {
        return this.errMsg;
    }
}
