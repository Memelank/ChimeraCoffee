package com.chimera.weapp.statemachine.enums;

public enum EventEnum {
    NEED_DINE_IN("需要堂食"),
    NEED_FIX_DELIVERY("需要指定派送"),
    SUPPLY_DINE_IN("提供堂食"),
    SUPPLY_FIX_DELIVERY("提供指定派送"),
    CANCEL_DINE_IN("商家取消堂食"),
    CANCEL_FIX_DELIVERY("商家取消指定派送"),
    CALL_AFTER_SALES("客户发起售后");

    EventEnum(String state) {
    }

}
