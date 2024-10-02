package com.chimera.weapp.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionValue {
    @NotNull
    private String uuid;
    private String value;
    private int priceAdjustment;
}
