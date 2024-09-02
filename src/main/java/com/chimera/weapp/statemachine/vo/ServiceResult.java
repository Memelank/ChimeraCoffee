package com.chimera.weapp.statemachine.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResult<T, C> {

    private T data;
    private C context;
    private String msg;
    private boolean isSuccess = false;

    public boolean isSuccess() {
        return isSuccess;
    }

}
