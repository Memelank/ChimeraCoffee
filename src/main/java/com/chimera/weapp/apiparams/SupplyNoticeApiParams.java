package com.chimera.weapp.apiparams;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
public class SupplyNoticeApiParams extends NoticeApiParams{
    @Schema(description = "订单状态")
    private String phrase16;
    @Schema(description = "温馨提示")
    private String thing7;
    @Schema(description = "门店名称")
    private String thing23;
    @Schema(description = "取餐地址")
    private String thing27;
    @Schema(description = "取餐号")
    private String character_string19;

}
