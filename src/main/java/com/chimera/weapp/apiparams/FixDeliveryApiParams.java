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
public class FixDeliveryApiParams {
    @Schema(description = "取货时间")
    private String time9;
    @Schema(description = "收货地址")
    private String thing17;
    @Schema(description = "联系人手机号")
    private String phone_number16;
    @Schema(description = "温馨提示")
    private String thing5;
    @Schema(description = "取餐码")
    private String character_string1;
}
