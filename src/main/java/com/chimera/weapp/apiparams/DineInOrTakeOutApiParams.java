package com.chimera.weapp.apiparams;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DineInOrTakeOutApiParams {
    @Schema(description = "订单状态")
    private String phrase19;
    @Schema(description = "温馨提示")
    private String thing11;
    @Schema(description = "门店名称")
    private String thing2;
    @Schema(description = "取餐地址")
    private String thing7;
    @Schema(description = "取餐号码")
    private String character_string4;
}
