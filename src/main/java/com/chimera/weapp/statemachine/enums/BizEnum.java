package com.chimera.weapp.statemachine.enums;

public enum BizEnum {
    FOR_PEKING_STUDENT("北大学生业务"),
    FOR_TSINGHUA_STUDENT("清华学生业务"),
    Not_Certified_As_A_Student("未认证为学生身份的用户业务");
    BizEnum(String businessTypes){

    }
    @Override
    public String toString() {
        return this.name();
    }
}
