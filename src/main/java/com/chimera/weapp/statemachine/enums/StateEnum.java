package com.chimera.weapp.statemachine.enums;

public enum StateEnum {
    PRE_PAID("预支付"),
    PAID("已支付"),
    WAITING_DINE_IN("待出餐"),
    WAITING_FIX_DELIVERY("待出餐"),
    WAITING_TAKE_OUT("待出餐"),
    NORMAL_END("正常结束"),
    CANCELED("已取消"),
    AFTERSALES("已售后"),
    REFUNDED("已退款");

    private final String state;

    // 构造函数接收自定义字符串
    StateEnum(String state) {
        this.state = state;
    }

    // 重写toString方法，返回自定义字符串
    @Override
    public String toString() {
        return this.state;
    }
}
