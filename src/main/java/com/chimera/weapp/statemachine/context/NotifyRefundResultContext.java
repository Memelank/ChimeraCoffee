package com.chimera.weapp.statemachine.context;

import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotifyRefundResultContext {
    RefundNotification refundNotification;
}
