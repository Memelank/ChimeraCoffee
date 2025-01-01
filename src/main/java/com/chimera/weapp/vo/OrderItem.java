package com.chimera.weapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Schema(description = "Product.id")
    private ObjectId productId;
    @Schema(description = "商品选项，key为ProductOption.id, value为一个完整的OptionValue")
    private Map<String, OptionValue> optionValues;
    @Schema(description = "Product.name")
    private String name;
    @Schema(description = "Product.cateId，用于判断优惠券是否可使用")
    private ObjectId cateId;
    @Schema(description = "根据Product.price和目前optionValues中OptionValue.priceAdjustment计算的价格。单位为分")
    private int price;
    @Schema(description = "Product.imgURL_small")
    private String imgURL_small;
}
