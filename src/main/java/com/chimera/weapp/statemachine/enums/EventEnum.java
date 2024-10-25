package com.chimera.weapp.statemachine.enums;

public enum EventEnum {
    NOTIFY_PRE_PAID("支付成功"),  //微信支付通过支付通知接口将用户支付成功消息通知给商户
    NEED_DINE_IN("需要堂食"),
    NEED_TAKE_OUT("需要外带"),
    NEED_FIX_DELIVERY("需要定时达"),
    SUPPLY_DINE_IN("提供堂食"),
    SUPPLY_TAKE_OUT("提供外带"),
    SUPPLY_FIX_DELIVERY("提供定时达"),
    REFUND("退款"),
    ;
    private final String state;

    // 构造函数接收自定义字符串
    EventEnum(String state) {
        this.state = state;
    }

    // 重写toString方法，返回自定义字符串
    @Override
    public String toString() {
        return this.state;
    }

}
