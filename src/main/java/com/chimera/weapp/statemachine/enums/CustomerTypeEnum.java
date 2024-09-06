package com.chimera.weapp.statemachine.enums;

public enum CustomerTypeEnum {
    FOR_PEKING_STUDENT("北大学生业务"),
    FOR_TSINGHUA_STUDENT("清华学生业务"),
    NOT_CERTIFIED_AS_A_STUDENT("未认证为学生身份的用户业务");
    CustomerTypeEnum(String businessTypes){

    }
    @Override
    public String toString() {
        return this.name();
    }
}
