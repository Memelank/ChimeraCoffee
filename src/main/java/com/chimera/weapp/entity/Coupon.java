package com.chimera.weapp.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Document(collection = "coupon")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    private ObjectId id;
    @Schema(description = "适用商品类，对应ProductCate.id，为空时适用所有商品类")
    private ObjectId cateId;
    @Schema(description = "抵扣金额")
    private int dePrice;
    @Schema(description = "对于delete=1的，不再允许发放、兑换")
    private int delete;
    @Schema(description = "消耗积分")
    private int costPoints;
    @Schema(description = "是否可用积分兑换")
    private boolean convertible;
    @Schema(description = "截至有效期")
    private Date validity;
}
