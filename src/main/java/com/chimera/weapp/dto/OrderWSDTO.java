package com.chimera.weapp.dto;

import com.chimera.weapp.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderWSDTO {
    String orderId;
    String state;

    public static OrderWSDTO.OrderWSDTOBuilder ofOrder(Order order){
        return OrderWSDTO.builder()
                .orderId(order.getId().toHexString())
                .state(order.getState());
    }

}
