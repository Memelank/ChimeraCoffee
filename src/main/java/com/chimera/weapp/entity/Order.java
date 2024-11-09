package com.chimera.weapp.entity;

import com.chimera.weapp.vo.CouponIns;
import com.chimera.weapp.vo.OrderItem;
import com.chimera.weapp.vo.DeliveryInfo;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "就是订单呀")
public class Order {
    @Id
    @NotNull
    private ObjectId id;
    @NotNull
    private ObjectId userId;
    @Schema(description = "自动填充状态")
    private String state;
    @Schema(description = "顾客类型，可选：\"北大学生业务\"，\"清华学生业务\"，\"未学生认证业务\"")
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
    private List<OrderItem> items;
    @Schema(description = "自动填充订单号")
    private int orderNum;
    @Schema(description = "顾客备注")
    private String remark;
    @Schema(description = "商家备注")
    private String merchantNote;
    @Schema(description = "后端根据sum(OrderItem.price)-coupon.dePrice计算，单位为分。")
    @NotNull
    private int totalPrice;

    @Schema(description = "本订单使用的优惠券，可为空")
    private CouponIns coupon;
    @Schema(description = "只给商品端使用的，线下优惠，小程序端传了也不处理。")
    private int disPrice = 0;

    @CreatedDate
    @Schema(description = "自动填充创建时间")
    private Date createdAt;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "异常结束原因")
    private String abNormalEndReason;
}
