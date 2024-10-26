package com.chimera.weapp.statemachine.context;

import com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotifyPrePayContext {
    private Transaction transaction;
}
