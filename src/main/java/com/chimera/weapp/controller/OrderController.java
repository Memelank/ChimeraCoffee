package com.chimera.weapp.controller;

import com.alibaba.fastjson2.JSONObject;
import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.dto.NotifyDTO;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.statemachine.context.*;
import com.chimera.weapp.statemachine.engine.OrderFsmEngine;

import com.chimera.weapp.statemachine.enums.EventEnum;
import com.chimera.weapp.statemachine.enums.SceneEnum;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderRepository repository;

    @Autowired
    private OrderFsmEngine orderFsmEngine;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private NotificationConfig notificationConfig;

    @GetMapping
    public List<Order> getAllOrders() {
        return repository.findAll();
    }

    @PostMapping("/wxcreate")
    @LoginRequired
    @Operation(summary = "创建预支付订单。小程序先调用这个，再调用wx.requestPayment")
    public JSONObject create(@RequestBody Order entity) {
        Order save = repository.save(entity);
        //https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi
        return null;
    }

    @PostMapping("/wxcreate_callback")
    @Operation(summary = "接收支付结果通知。是腾讯的微信支付系统调用的")
    public ResponseEntity<ServiceResult> callback(@RequestBody String requestBody) throws Exception {
        RequestParam requestParam = buildRequestParam(requestBody);
        NotificationParser parser = new NotificationParser(notificationConfig);
        Transaction transaction = parser.parse(requestParam, Transaction.class);
        String outTradeNo = transaction.getOutTradeNo();

        Optional<Order> order = repository.findById(new ObjectId(outTradeNo));
        if (order.isEmpty()) {
            throw new RuntimeException("根据outTradeNo未查询到订单");
        }
        Order save = order.get();
        ServiceResult<Object, ?> serviceResult = null;

        StateContext<Object> context = new StateContext<>();
        setNormalContext(context, save);
        if (SceneEnum.FIX_DELIVERY.toString().equals(save.getScene())) {
            FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
            context.setContext(fixDeliveryContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_FIX_DELIVERY.toString(), context);
        } else if (SceneEnum.DINE_IN.toString().equals(save.getScene())) {
            DineInContext dineInContext = new DineInContext();
            context.setContext(dineInContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_DINE_IN.toString(), context);
        } else if (SceneEnum.TAKE_OUT.toString().equals(save.getScene())) {
            TakeOutContext takeOutContext = new TakeOutContext();
            context.setContext(takeOutContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_TAKE_OUT.toString(), context);
        }

        if (serviceResult != null && serviceResult.isSuccess()) {
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }
    }

    private RequestParam buildRequestParam(String requestBody) {
        String wechatPaySerial = httpServletRequest.getHeader("Wechatpay-Serial");
        String wechatpayNonce = httpServletRequest.getHeader("Wechatpay-Nonce");
        String wechatSignature = httpServletRequest.getHeader("Wechatpay-Signature");
        String wechatTimestamp = httpServletRequest.getHeader("Wechatpay-Timestamp");
        return new RequestParam.Builder()
                .serialNumber(wechatPaySerial)
                .nonce(wechatpayNonce)
                .signature(wechatSignature)
                .timestamp(wechatTimestamp)
                .body(requestBody)
                .build();
    }

    @PostMapping("/supply")
    @LoginRequired
    public ResponseEntity<ServiceResult> supplyOrder(@RequestBody Order order) throws Exception {
        ServiceResult<Object, ?> serviceResult = null;

        System.out.println("Order process api get.");
        System.out.println("OrderId:" + order.getId().toString());
        System.out.println("UserId:" + order.getUserId().toString());
        System.out.println("State:" + order.getState().toString());

        StateContext<Object> context = new StateContext<>();
        setNormalContext(context, order);
        if (SceneEnum.FIX_DELIVERY.toString().equals(order.getScene())) {
            FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
            context.setContext(fixDeliveryContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.SUPPLY_FIX_DELIVERY.toString(), context);
        } else if (SceneEnum.DINE_IN.toString().equals(order.getScene())) {
            DineInContext dineInContext = new DineInContext();
            context.setContext(dineInContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.SUPPLY_DINE_IN.toString(), context);
        } else if (SceneEnum.TAKE_OUT.toString().equals(order.getScene())) {
            TakeOutContext takeOutContext = new TakeOutContext();
            context.setContext(takeOutContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.SUPPLY_TAKE_OUT.toString(), context);
        }

        if (serviceResult != null && serviceResult.isSuccess()) {
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }
    }

    @PostMapping("/cancel")
    @LoginRequired
    public ResponseEntity<ServiceResult> cancelOrder(@RequestBody Order order) throws Exception {
        ServiceResult<Object, ?> serviceResult = null;

        StateContext<Object> context = new StateContext<>();
        setNormalContext(context, order);
        if (SceneEnum.FIX_DELIVERY.toString().equals(order.getScene())) {
            FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
            context.setContext(fixDeliveryContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.CANCEL_FIX_DELIVERY.toString(), context);
        } else if (SceneEnum.DINE_IN.toString().equals(order.getScene())) {
            DineInContext dineInContext = new DineInContext();
            context.setContext(dineInContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.CANCEL_DINE_IN.toString(), context);
        } else if (SceneEnum.TAKE_OUT.toString().equals(order.getScene())) {
            TakeOutContext takeOutContext = new TakeOutContext();
            context.setContext(takeOutContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.CANCEL_TAKE_OUT.toString(), context);
        }

        if (serviceResult != null && serviceResult.isSuccess()) {
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }
    }

    @PostMapping("/after_sale")
    @LoginRequired
    public ResponseEntity<ServiceResult> afterSale(@RequestBody Order order) throws Exception {
        ServiceResult<Object, ?> serviceResult = null;

        StateContext<Object> context = new StateContext<>();
        setNormalContext(context, order);
        CallAfterSalesContext callAfterSalesContext = new CallAfterSalesContext();
        context.setContext(callAfterSalesContext);
        serviceResult = orderFsmEngine.sendEvent(EventEnum.CALL_AFTER_SALES.toString(), context);

        if (serviceResult != null && serviceResult.isSuccess()) {
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }
    }

    private void setNormalContext(StateContext<?> context, Order save) {
        context.setOrderId(save.getId().toString());
        context.setUserId(save.getUserId().toString());
        context.setOrderState(save.getState());
        context.setCustomerType(save.getCustomerType());
        context.setScene(save.getScene());
    }
}
