package com.chimera.weapp.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoffeeVariant {
    private String size;
    private String temperature;
    private String intensity;
    private double price;
}
