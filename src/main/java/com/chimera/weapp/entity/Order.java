package com.chimera.weapp.entity;

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
    private ObjectId id;
    @NotNull
    private ObjectId userId;
    @Schema(description = "自动填充状态")
    private String state;
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
    private List<OrderItem> items;
    @Schema(description = "自动填充订单号")
    private int orderNum;
    @Schema(description = "顾客备注")
    private String remark;
    @Schema(description = "商家备注")
    private String merchantNote;
    @Schema(description = "前端先计算一个，根据sum(OrderItem.price)-优惠券，后端会check")
    @NotNull
    private int totalPrice;

    @CreatedDate
    @Schema(description = "自动填充创建时间")
    private Date createdAt;
}
