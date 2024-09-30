package com.chimera.weapp.statemachine.enums;

public enum EventEnum {
    NEED_DINE_IN("需要堂食"),
    NEED_TAKE_OUT("需要外带"),
    NEED_FIX_DELIVERY("需要定时达"),
    SUPPLY_DINE_IN("提供堂食"),
    SUPPLY_TAKE_OUT("提供外带"),
    SUPPLY_FIX_DELIVERY("提供定时达"),
    CANCEL_DINE_IN("商家取消堂食"),
    CANCEL_FIX_DELIVERY("商家取消定时达"),
    CANCEL_TAKE_OUT("商家取消外带"),
    CALL_AFTER_SALES("客户发起售后");

    EventEnum(String state) {
    }

}
