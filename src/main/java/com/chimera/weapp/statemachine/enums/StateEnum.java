package com.chimera.weapp.statemachine.enums;

public enum StateEnum {
    PAID("已支付"),
    WAITING_DINE_IN("待出餐"),
    WAITING_FIX_DELIVERY("待出餐"),
    NORMAL_END("正常结束"),
    ABNORMAL_END("异常结束")
    ;

    StateEnum(String state) {
    }

    @Override
    public String toString() {
        return this.name();
    }
}
