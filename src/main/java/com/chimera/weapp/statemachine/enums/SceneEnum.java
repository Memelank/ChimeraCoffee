package com.chimera.weapp.statemachine.enums;

public enum SceneEnum {
    DINE_IN("堂食"),
    TAKE_OUT("外带"),
    FIX_DELIVERY("定时达"),
    SCHOOL_ANNIVERSARY("校庆场景");

    private final String scene;

    SceneEnum(String scene) {
        this.scene = scene;
    }

    @Override
    public String toString() {
        return this.scene;
    }

}
