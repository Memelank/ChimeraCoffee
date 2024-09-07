package com.chimera.weapp.statemachine.enums;

public enum SceneEnum {
    DINE_IN("堂食"),
    FIX_DELIVERY("指定出餐"),
    SCHOOL_ANNIVERSARY("校庆场景");

    SceneEnum(String scene) {

    }

    @Override
    public String toString() {
        return this.name();
    }
}
