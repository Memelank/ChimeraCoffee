package com.chimera.weapp.statemachine.enums;

public enum SceneEnum {
    Promotion("促销场景"),
    School_Anniversary("校庆场景"),
    Common("日常场景");
    SceneEnum(String scene){

    }
    @Override
    public String toString() {
        return this.name();
    }
}
