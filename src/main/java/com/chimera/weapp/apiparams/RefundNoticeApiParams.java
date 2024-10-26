package com.chimera.weapp.apiparams;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class RefundNoticeApiParams {
    @Schema(description = "订单编号")
    private String character_string1;
    @Schema(description = "退款金额")
    private String amount2;
    @Schema(description = "退款时间")
    private String time3;
    @Schema(description = "退款原因")
    private String thing4;
    @Schema(description = "温馨提示")
    private String thing5;

}

