package com.chimera.weapp.statemachine.context;

import com.chimera.weapp.vo.DeliveryInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FixDeliveryContext {
    DeliveryInfo deliveryInfo;
}