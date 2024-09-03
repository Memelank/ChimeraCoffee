package com.chimera.weapp.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    private ObjectId productId;
    private String size;
    private String heat;
    private String intensity;
    private int quantity;
    private double price;
}
