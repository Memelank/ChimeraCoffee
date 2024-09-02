package com.chimera.weapp.statemachine.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InitOrderContext<T> {
    private T estimatePriceInfo; //促销信息
}