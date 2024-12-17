package com.chimera.weapp.apiparams;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
public class RefundNoticeApiParams extends NoticeApiParams{
    @Schema(description = "订单编号")
    private String number1;
    @Schema(description = "退款金额")
    private String amount3;
    @Schema(description = "退款时间")
    private String time2;
    @Schema(description = "退款原因")
    private String thing4;
    @Schema(description = "温馨提示")
    private String thing8;

}

