package com.chimera.weapp.statemachine.enums;

public enum StateEnum {
    PAID("已支付"),
    WAITING_DINE_IN("待出餐"),
    WAITING_FIX_DELIVERY("待出餐"),
    WAITING_TAKE_OUT("待出餐"),
    NORMAL_END("正常结束"),
    ABNORMAL_END("异常结束");

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
