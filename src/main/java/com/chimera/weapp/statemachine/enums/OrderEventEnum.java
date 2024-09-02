package com.chimera.weapp.statemachine.enums;

public enum OrderEventEnum {
    INIT("初始化"),
    ;

    OrderEventEnum(String state) {
    }

    @Override
    public String toString() {
        return this.name();
    }

}
