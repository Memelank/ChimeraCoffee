package com.chimera.weapp.statemachine.enums;

public enum CustomerTypeEnum {
    FOR_PEKING_STUDENT("北大学生业务"),
    FOR_TSINGHUA_STUDENT("清华学生业务"),
    NOT_CERTIFIED_AS_A_STUDENT("未认证为学生身份的用户业务");

    private final String businessTypes;

    // 构造函数接收自定义字符串
    CustomerTypeEnum(String businessTypes) {
        this.businessTypes = businessTypes;
    }

    // 重写toString方法，返回自定义字符串
    @Override
    public String toString() {
        return this.businessTypes;
    }
}
