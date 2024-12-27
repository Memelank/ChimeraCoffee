package com.chimera.weapp.apiparams;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
public class SupplyNoticeApiParams extends NoticeApiParams{
    @Schema(description = "取餐号")
    private String character_string19;
    @Schema(description = "餐品详情")
    private String thing11;
    @Schema(description = "取餐地址")
    private String thing27;
    @Schema(description = "联系电话")
    private String phone_number32;
    @Schema(description = "温馨提醒")
    private String thing7;
}
