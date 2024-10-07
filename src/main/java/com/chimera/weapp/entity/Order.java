package com.chimera.weapp.entity;

import com.chimera.weapp.vo.OrderItem;
import com.chimera.weapp.vo.DeliveryInfo;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private ObjectId userId;
    private String state;
    @Schema(description = "如\"未认证为学生身份的用户业务\"")
    private String customerType;
    private String scene;
    @Schema(description = "定时达配送信息，暂不用填")
    private DeliveryInfo deliveryInfo;
    @ArraySchema(arraySchema = @Schema(description = "订单所含商品列表", example = "[example1,example2...]"),
            schema = @Schema(description = "订单其中的一个商品"))
    private List<OrderItem> items;
    private int orderNum;
    @Schema(description = "顾客备注")
    private String remark;
    private String merchantNote;
    @Schema(description = "前端先计算一个，后端会check")
    private int totalPrice;

    @CreatedDate
    @Schema(description = "自动填充创建时间")
    private Date createdAt;
}
