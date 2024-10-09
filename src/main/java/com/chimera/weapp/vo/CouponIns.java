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
public class CouponIns {
    @NotNull
    private String uuid;
    @Schema(description = "对应Coupon.id")
    private String couponId;
    @Schema(description = "0=未使用，1=已使用，-1=已过期")
    private int status;
    @Schema(description = "抵扣金额，与对应Coupon.dePrice对应")
    private int dePrice;
}
