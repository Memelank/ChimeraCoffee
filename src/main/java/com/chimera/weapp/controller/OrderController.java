package com.chimera.weapp.controller;

import com.alibaba.fastjson2.JSONObject;
import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.service.OrderService;
import com.chimera.weapp.service.WeChatService;
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
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository repository;

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private OrderFsmEngine orderFsmEngine;

    @Autowired
    private HttpServletRequest httpServletRequest;

//    @Autowired
//    private NotificationConfig notificationConfig;

    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @org.springframework.web.bind.annotation.RequestParam("startTime") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date startTime,
            @org.springframework.web.bind.annotation.RequestParam("endTime") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date endTime) {

        // 检查开始时间是否大于或等于结束时间
        if (startTime.compareTo(endTime) >= 0) {
            return ResponseEntity.badRequest().body("开始时间必须小于结束时间");
        }

        // 查找在指定时间范围内的订单，并按createdAt时间从近到远排序
        List<Order> orders = repository.findByCreatedAtBetweenOrderByCreatedAtDesc(startTime, endTime);

        return ResponseEntity.ok(orders);
    }



    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUserId(@PathVariable String userId) {
        ObjectId userObjectId = new ObjectId(userId);
        return repository.findByUserIdOrderByCreatedAtDesc(userObjectId);
    }

    @PostMapping("/wxcreate")
    @LoginRequired
    @Operation(summary = "创建预支付订单。小程序先调用这个，再调用wx.requestPayment")
    public JSONObject create(@RequestBody Order order) throws URISyntaxException, IOException {
        order.setState(StateEnum.PRE_PAID.toString());
        orderService.checkPrice(order);
        Order save = repository.save(order);
        return weChatService.jsapiTransaction(save);
    }


    @PostMapping("/wxcreate_callback")
    @Operation(summary = "接收支付结果通知。是腾讯的微信支付系统调用的")
    public ResponseEntity<String> callback(@RequestBody String requestBody) throws Exception {
//        NotificationParser parser = new NotificationParser(notificationConfig);
        NotificationParser parser = new NotificationParser();
        RequestParam requestParam = buildRequestParam(requestBody);
        Transaction transaction = parser.parse(requestParam, Transaction.class);
        String outTradeNo = transaction.getOutTradeNo();
        synchronized (outTradeNo.intern()) {
            Optional<Order> order = repository.findById(new ObjectId(outTradeNo));
            if (order.isEmpty()) {
                throw new RuntimeException("根据outTradeNo未查询到订单");
            }
            Order save = order.get();
            if (!Objects.equals(save.getState(), StateEnum.PRE_PAID.toString()) &&
                    !Objects.equals(save.getScene(), StateEnum.PAID.toString())) {
                log.info("微信重复发送通知给订单号为{}的订单,已默认成功", outTradeNo);
                return ResponseEntity.ok("");
            }
            StateContext<Object> context = new StateContext<>();
            setNormalContext(context, save);

            PrePayContext prePayContext = new PrePayContext();
            context.setContext(prePayContext);
            orderFsmEngine.sendEvent(EventEnum.NOTIFY_PRE_PAID.toString(), context);
            ServiceResult<Object, ?> serviceResult = new ServiceResult<>();
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

            if (serviceResult.isSuccess()) {
                return ResponseEntity.ok("");
            } else {
                return ResponseEntity.internalServerError().body(
                        String.format("{\"code\":\"FAIL\",\"message\":\"%s\"}", serviceResult.getMsg())
                );
            }
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

    @PostMapping("/create")
    @LoginRequired
    public ResponseEntity<ServiceResult> createOrderInStore(@RequestBody Order entity) throws Exception {
        // 设置订单状态为已支付
        entity.setState(StateEnum.PAID.toString());

        // 获取当前日期的开始时间（0点）
        Date startOfDay = getStartOfDay(new Date());

        // 查询当天的订单数量
        long orderCountToday = repository.countByCreatedAtGreaterThanEqual(startOfDay);

        // 设置订单号，从1开始累计
        entity.setOrderNum((int) orderCountToday + 1);

        // 保存订单
        Order save = repository.save(entity);

        // FSM 状态机处理
        ServiceResult<Object, ?> serviceResult = null;
        StateContext<Object> context = new StateContext<>();
        setNormalContext(context, save);

        // 根据不同的场景设置上下文并发送事件
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

        // 根据状态机的处理结果返回不同的响应
        if (serviceResult != null && serviceResult.isSuccess()) {
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }
    }

    /**
     * 获取当天开始时间（0点）
     */
    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @PostMapping(value = "/supply", consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    public ResponseEntity<ServiceResult> supplyOrder(@RequestBody Order order) throws Exception {
        ServiceResult<Object, ?> serviceResult = null;

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

    @PostMapping(value = "/refund", consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    public ResponseEntity<ServiceResult> refundOrder(@RequestBody Order save) throws Exception {

        Order order = repository.save(save);
        ServiceResult<Object, ?> serviceResult = null;

        StateContext<Object> context = new StateContext<>();
        setNormalContext(context, order);
        RefundContext refundContext = new RefundContext();
        context.setContext(refundContext);
        serviceResult = orderFsmEngine.sendEvent(EventEnum.REFUND.toString(), context);

        if (serviceResult != null && serviceResult.isSuccess()) {
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }

//        ServiceResult<Object, ?> serviceResult = null;
//
//        StateContext<Object> context = new StateContext<>();
//        setNormalContext(context, order);
//
//        System.out.println("context:" + context);
//        if (SceneEnum.FIX_DELIVERY.toString().equals(order.getScene())) {
//            FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
//            context.setContext(fixDeliveryContext);
//            serviceResult = orderFsmEngine.sendEvent(EventEnum.CANCEL_FIX_DELIVERY.toString(), context);
//        } else if (SceneEnum.DINE_IN.toString().equals(order.getScene())) {
//            DineInContext dineInContext = new DineInContext();
//            context.setContext(dineInContext);
//            serviceResult = orderFsmEngine.sendEvent(EventEnum.CANCEL_DINE_IN.toString(), context);
//        } else if (SceneEnum.TAKE_OUT.toString().equals(order.getScene())) {
//            TakeOutContext takeOutContext = new TakeOutContext();
//            context.setContext(takeOutContext);
//            serviceResult = orderFsmEngine.sendEvent(EventEnum.CANCEL_TAKE_OUT.toString(), context);
//        }
//
//        if (serviceResult != null && serviceResult.isSuccess()) {
//            return ResponseEntity.ok(serviceResult);
//        } else {
//            return ResponseEntity.internalServerError().body(serviceResult);
//        }
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
