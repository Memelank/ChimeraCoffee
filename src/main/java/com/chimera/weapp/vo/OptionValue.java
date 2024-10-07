package com.chimera.weapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "加购商品的可选项的可选值，如对于\"规格\"，value可以为\"中杯\"")
    private String value;
    @Schema(description = "该可选项的价格调整，>=0")
    private int priceAdjustment;
}
