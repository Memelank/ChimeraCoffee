package com.chimera.weapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponIns {
    @NotNull
    private String uuid;
    @Schema(description = "对应Coupon.id")
    private String couponId;
    @Schema(description = "Coupon.name")
    private String name;
    @Schema(description = "0=未使用，1=已使用")
    private int status;
    @Schema(description = "适用商品类，对应ProductCate.id，为空时适用所有商品类")
    private String cateId;
    @Schema(description = "抵扣金额，与对应Coupon.dePrice对应。单位为分")
    private int dePrice;
    @Schema(description = "Coupon.validity")
    private Date validity;
}
