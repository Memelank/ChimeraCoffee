package com.chimera.weapp.controller;

import com.alibaba.fastjson2.JSONObject;
import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.apiparams.OrderApiParams;
import com.chimera.weapp.config.WebSocketConfig;
import com.chimera.weapp.dto.BatchSupplyOrderDTO;
import com.chimera.weapp.dto.PrePaidDTO;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.ProductRepository;
import com.chimera.weapp.service.BenefitService;
import com.chimera.weapp.service.OrderService;
import com.chimera.weapp.service.SecurityService;
import com.chimera.weapp.service.WeChatRequestService;
import com.chimera.weapp.statemachine.context.*;
import com.chimera.weapp.statemachine.engine.OrderFsmEngine;
import com.chimera.weapp.statemachine.enums.EventEnum;
import com.chimera.weapp.statemachine.enums.SceneEnum;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
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
    private ProductRepository productRepository;

    @Autowired
    private WeChatRequestService weChatRequestService;

    @Autowired
    private OrderFsmEngine orderFsmEngine;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private BenefitService benefitService;

//    @Autowired
//    private NotificationConfig notificationConfig;

    @Autowired
    private WebSocketConfig webSocketConfig;

    @GetMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
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

    @GetMapping("/getOrdersByDeliveryInfo")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<?> getOrdersByDeliveryInfo(
            @org.springframework.web.bind.annotation.RequestParam String school,
            @org.springframework.web.bind.annotation.RequestParam String address,
            @org.springframework.web.bind.annotation.RequestParam String time,
            @org.springframework.web.bind.annotation.RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        try {
            // 设置开始和结束时间为当天
            Date startTime = date;
            startTime.setHours(0);
            startTime.setMinutes(0);
            startTime.setSeconds(0);

            Date endTime = date;
            endTime.setHours(23);
            endTime.setMinutes(59);
            endTime.setSeconds(59);

            // 查找符合条件的订单
            List<Order> orders = repository.findByDeliveryInfoAndCreatedAtBetween(
                    school, address, time, startTime, endTime
            );

            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("根据配送信息查找订单时发生错误:", e);
            return ResponseEntity.internalServerError().body("获取订单失败：" + e.getMessage());
        }
    }


    @GetMapping("/user/{userId}")
    @Operation(summary = "根据userId查询Orders，当前端传递all=true时，返回所有，否则默认10条")
    public ResponseEntity<List<Order>> getOrdersByUserId(
            @PathVariable String userId,
            @org.springframework.web.bind.annotation.RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {

        ObjectId userObjectId = new ObjectId(userId);

        // 判断是否需要获取所有订单
        if (all) {
            // 获取所有订单，并按时间倒序排列
            return ResponseEntity.ok(repository.findByUserIdOrderByCreatedAtDesc(userObjectId));
        } else {
            // 仅获取最近的5个订单，并按时间倒序排列
            return ResponseEntity.ok(repository.findTop10ByUserIdOrderByCreatedAtDesc(userObjectId));
        }
    }


    @PostMapping("/wxcreate")
    @LoginRequired
    @Operation(summary = "创建预支付订单。小程序先调用这个，再调用wx.requestPayment")
    public PrePaidDTO create(@RequestBody OrderApiParams orderApiParams) throws URISyntaxException, IOException {
        securityService.checkIdImitate(orderApiParams.getUserId());
        Order order = orderService.buildOrderByApiParams(orderApiParams);
        order.setState(StateEnum.PRE_PAID.toString());
        Order save = repository.save(order);
        PrePaidDTO prePaidDTO = weChatRequestService.jsapiTransaction(save);
        webSocketConfig.getOrderCreateWebSocketHandler().sendOrderId(order.getId().toHexString());
        return prePaidDTO;
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
            //第一次调用状态机。发送PRE_PAID事件
            Order order = repository.findById(new ObjectId(outTradeNo)).orElseThrow();
            if (Objects.equals(order.getState(), StateEnum.PRE_PAID.toString()) ||
                    Objects.equals(order.getScene(), StateEnum.PAID.toString())) {
                log.warn("微信重复发送通知给订单号为{}的订单,已默认成功", outTradeNo);
                return ResponseEntity.ok("");
            }
            StateContext<Object> context = new StateContext<>();
            setNormalContext(context, order);

            PrePayContext prePayContext = new PrePayContext();
            context.setContext(prePayContext);
            ServiceResult<Object, Object> prePaidFSMResult = orderFsmEngine.sendEvent(EventEnum.NOTIFY_PRE_PAID.toString(), context);
            if (!prePaidFSMResult.isSuccess()) {
                return ResponseEntity.internalServerError().body(
                        String.format("{\"code\":\"FAIL\",\"message\":\"%s\"}", prePaidFSMResult.getMsg())
                );
            }
            //核销优惠
            if (order.getCoupon() != null) {
                String orderCouponUUID = order.getCoupon().getUuid();
                ObjectId userId = order.getUserId();
                benefitService.redeemUserCoupon(userId, orderCouponUUID);
            }

            //第二次调用状态机。从PAID状态转变
            Order order1 = repository.findById(new ObjectId(outTradeNo)).orElseThrow();
            ServiceResult<Object, ?> serviceResult = new ServiceResult<>();
            if (SceneEnum.FIX_DELIVERY.toString().equals(order1.getScene())) {
                FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
                context.setContext(fixDeliveryContext);
                serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_FIX_DELIVERY.toString(), context);
            } else if (SceneEnum.DINE_IN.toString().equals(order1.getScene())) {
                DineInContext dineInContext = new DineInContext();
                context.setContext(dineInContext);
                serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_DINE_IN.toString(), context);
            } else if (SceneEnum.TAKE_OUT.toString().equals(order1.getScene())) {
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
    @LoginRequired // TODO 上线要限制ADMIN
    @Operation(summary = "用于商铺端创建订单，不走微信支付，微信支付未办理前小程序也可先调用这个")
    public ResponseEntity<ServiceResult> createOrderInStore(@RequestBody OrderApiParams orderApiParams) throws Exception {
        securityService.checkIdImitate(orderApiParams.getUserId());
        Order order = orderService.buildOrderByApiParamsShop(orderApiParams);
        order.setState(StateEnum.PRE_PAID.toString());
        // 保存订单
        Order save = repository.save(order);
        // FSM 状态机处理
        //1.预支付到支付状态，过一遍积分的逻辑
        StateContext<Object> context = new StateContext<>();
        setNormalContext(context, save);
        PrePayContext prePayContext = new PrePayContext();
        context.setContext(prePayContext);
        orderFsmEngine.sendEvent(EventEnum.NOTIFY_PRE_PAID.toString(), context);

        //核销优惠  TODO:上线后可去掉
        if (order.getCoupon() != null) {
            String orderCouponUUID = order.getCoupon().getUuid();
            ObjectId userId = order.getUserId();
            benefitService.redeemUserCoupon(userId, orderCouponUUID);
        }

        //2.支付状态到其它别的状态
        Order orderAfterFirstProcess = repository.findById(save.getId()).orElseThrow();
        setNormalContext(context, orderAfterFirstProcess);
        ServiceResult<Object, ?> serviceResult = null;
        // 根据不同的场景设置上下文并发送事件
        if (SceneEnum.FIX_DELIVERY.toString().equals(orderAfterFirstProcess.getScene())) {
            FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
            context.setContext(fixDeliveryContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_FIX_DELIVERY.toString(), context);
        } else if (SceneEnum.DINE_IN.toString().equals(orderAfterFirstProcess.getScene())) {
            DineInContext dineInContext = new DineInContext();
            context.setContext(dineInContext);
            System.out.println(context);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_DINE_IN.toString(), context);
        } else if (SceneEnum.TAKE_OUT.toString().equals(orderAfterFirstProcess.getScene())) {
            TakeOutContext takeOutContext = new TakeOutContext();
            context.setContext(takeOutContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_TAKE_OUT.toString(), context);
        }

        // 根据状态机的处理结果返回不同的响应
        if (serviceResult != null && serviceResult.isSuccess()) {
            webSocketConfig.getOrderCreateWebSocketHandler().sendOrderId(save.getId().toHexString());//要调试的话可以把这个和查单的鉴权注解注释掉，再用postman调建新单接口
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }
    }


    @PostMapping(value = "/supply", consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
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
            webSocketConfig.getOrderUpdateWebSocketHandler().sendMessageToOrder(order.getId().toHexString(), "已供餐");
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }
    }

    @PostMapping("/batchSupply")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Map<String, Object>> batchSupplyOrders(@RequestBody BatchSupplyOrderDTO request) {
        List<String> orderIds = request.getOrderIds();
        Map<String, Object> response = new HashMap<>();

        try {
            for (String orderId : orderIds) {
                Optional<Order> orderOptional = repository.findById(new ObjectId(orderId));
                if (orderOptional.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "订单 " + orderId + " 未找到");
                    return ResponseEntity.badRequest().body(response);
                }

                Order order = orderOptional.get();

                StateContext<Object> context = new StateContext<>();
                setNormalContext(context, order);

                FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
                context.setContext(fixDeliveryContext);
                ServiceResult<Object, ?> serviceResult = orderFsmEngine.sendEvent(EventEnum.SUPPLY_FIX_DELIVERY.toString(), context);

                if (!serviceResult.isSuccess()) {
                    response.put("success", false);
                    response.put("message", serviceResult.getMsg());
                    return ResponseEntity.internalServerError().body(response);
                }
            }

            response.put("success", true);
            response.put("message", "所有订单成功供餐");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("批量供餐时发生错误:", e);
            response.put("success", false);
            response.put("message", "批量供餐失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping(value = "/refund", consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<ServiceResult> refundOrder(@RequestBody Order save) throws Exception {
        Order order = repository.save(save);
        ServiceResult<Object, ?> serviceResult = null;

        StateContext<Object> context = new StateContext<>();
        setNormalContext(context, order);
        RefundContext refundContext = new RefundContext();//todo 往refundContext上添加退款原因
        context.setContext(refundContext);
        serviceResult = orderFsmEngine.sendEvent(EventEnum.REFUND.toString(), context);

        if (serviceResult != null && serviceResult.isSuccess()) {
            webSocketConfig.getOrderUpdateWebSocketHandler().sendMessageToOrder(order.getId().toHexString(), "已退款");
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
