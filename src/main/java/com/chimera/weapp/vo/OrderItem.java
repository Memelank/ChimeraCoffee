package com.chimera.weapp.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    private ObjectId productId;
    private Map<Integer, String> optionIdToValue;
    private int quantity;
    private double price;
}
