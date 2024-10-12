package com.chimera.weapp.apiparams;

import com.chimera.weapp.vo.DeliveryInfo;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderApiParams {
    @NotNull
    private ObjectId userId;
    @Schema(description = "顾客类型，可选：\"北大学生业务\"，\"清华学生业务\"，\"未认证为学生身份的用户业务\"")
    @NotNull
    private String customerType;
    @Schema(description = "场景，可选：\"堂食\"，\"外带\"，\"定时达\"")
    @NotNull
    private String scene;
    @Schema(description = "定时达配送信息")
    private DeliveryInfo deliveryInfo;
    @ArraySchema(arraySchema = @Schema(description = "订单所含商品列表", example = "[example1,example2...]"),
            schema = @Schema(description = "订单其中的一个商品"))
    @NotNull
    private List<OrderItemApiParams> items;
    @Schema(description = "顾客备注")
    private String remark;
    @Schema(description = "商家备注")
    private String merchantNote;
    @Schema(description = "只给商品端使用的，线下优惠，小程序端传了也不处理。")
    private int disPrice;
    @Schema(description = "本订单使用的优惠券uuid，可为空")
    private String couponInsUUID;
}
