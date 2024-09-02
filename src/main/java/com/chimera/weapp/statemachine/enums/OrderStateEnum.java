package com.chimera.weapp.statemachine.enums;

public enum OrderStateEnum {
    TO_BE_INIT("待初始化"),
    INITIALIZED("已初始化"),
    ;

    OrderStateEnum(String state) {
    }

    @Override
    public String toString() {
        return this.name();
    }
}
