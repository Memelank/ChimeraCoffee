package com.chimera.weapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryInfo {
    private String school;
    @Schema(description = "绑定到具体订单实例的已选地址")
    private String address;
    @Schema(description = "绑定到具体订单实例的已选时间，可以是当天也可以是第二天。")
    private Date time;
}
