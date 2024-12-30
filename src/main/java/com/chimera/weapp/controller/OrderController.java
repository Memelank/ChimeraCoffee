package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.apiparams.OrderApiParams;
import com.chimera.weapp.apiparams.RefundApplyApiParams;
import com.chimera.weapp.config.WebSocketConfig;
import com.chimera.weapp.dto.BatchSupplyOrderDTO;
import com.chimera.weapp.dto.ResponseBodyDTO;
import com.chimera.weapp.entity.AppConfiguration;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.AppConfigurationRepository;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.service.*;
import com.chimera.weapp.statemachine.context.*;
import com.chimera.weapp.statemachine.engine.OrderFsmEngine;
import com.chimera.weapp.statemachine.enums.EventEnum;
import com.chimera.weapp.statemachine.enums.SceneEnum;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import com.chimera.weapp.vo.DeliveryInfo;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private UserRepository userRepository;

    @Autowired
    private AppConfigurationRepository appConfigurationRepository;

    @Autowired
    private CustomRepository customRepository;

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

    @Autowired
    private NotificationConfig notificationConfig;

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Autowired
    private WechatPaymentService wechatPaymentService;

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

    @GetMapping("/getById")
    @LoginRequired
    public ResponseEntity<?> getOrderById(@org.springframework.web.bind.annotation.RequestParam String orderId) {
        Map<String, Object> response = new HashMap<>();

        // Validate the orderId to ensure it's a valid format
        if (orderId == null || orderId.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Invalid order ID");
            return ResponseEntity.badRequest().body(response);
        }

        // Convert orderId to ObjectId for MongoDB query
        ObjectId objectId;
        try {
            objectId = new ObjectId(orderId);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", "Invalid order ID format");
            return ResponseEntity.badRequest().body(response);
        }

        // Fetch the order from the repository by its ID
        Optional<Order> orderOptional = repository.findById(objectId);

        if (orderOptional.isPresent()) {
            // Return the order if found
            return ResponseEntity.ok(orderOptional.get());
        } else {
            // Return 404 if the order is not found
            response.put("status", "error");
            response.put("message", "Order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    @GetMapping("/getOrdersByDeliveryInfo")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<?> getOrdersByDeliveryInfo(
            @org.springframework.web.bind.annotation.RequestParam String school,
            @org.springframework.web.bind.annotation.RequestParam String address,
            @org.springframework.web.bind.annotation.RequestParam String time) {
        try {
            // 解析 "HH:mm" 格式的时间字符串，并与当天日期组合
            Date targetTime = parseTimeStringToDate(time);

            // 查找符合条件的订单
            List<Order> orders = repository.findByDeliveryInfoSchoolAndDeliveryInfoAddressAndDeliveryInfoTimeAndState(
                    school, address, targetTime, StateEnum.WAITING_FIX_DELIVERY.toString()
            );

            log.info("查询到 {} 条符合条件的订单", orders.size());

            return ResponseEntity.ok(orders);
        } catch (ParseException e) {
            log.error("时间格式解析错误:", e);
            return ResponseEntity.badRequest().body("时间格式错误，正确格式为 HH:mm");
        } catch (Exception e) {
            log.error("根据配送信息查找订单时发生错误:", e);
            return ResponseEntity.internalServerError().body("获取订单失败：" + e.getMessage());
        }
    }

    private Date parseTimeStringToDate(String timeStr) throws ParseException {
        // 获取当天的日期部分
        Calendar today = Calendar.getInstance();
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // 解析 "HH:mm" 部分
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date timePart = timeFormat.parse(timeStr);

        // 获取时间部分的小时和分钟
        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(timePart);
        int hour = timeCal.get(Calendar.HOUR_OF_DAY);
        int minute = timeCal.get(Calendar.MINUTE);

        // 将时间部分设置到当天日期，秒和毫秒设为0
        today.set(Calendar.HOUR_OF_DAY, hour);
        today.set(Calendar.MINUTE, minute);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today.getTime();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据userId查询Orders，当前端传递all=true时，返回所有，否则默认10条")
    public ResponseEntity<List<Order>> getOrdersByUserId(
            @PathVariable String userId,
            @org.springframework.web.bind.annotation.RequestParam(value = "all", required = false, defaultValue = "false") boolean all,
            @org.springframework.web.bind.annotation.RequestParam(value = "newest", required = false, defaultValue = "false") boolean newest) {

        ObjectId userObjectId = new ObjectId(userId);

        // 判断是否需要获取所有订单
        if (all) {
            // 获取所有订单，并按时间倒序排列
            return ResponseEntity.ok(repository.findByUserIdOrderByCreatedAtDesc(userObjectId));
        } else if (newest) {
            //返回最新
            return ResponseEntity.ok(repository.findTop1ByUserIdOrderByCreatedAtDesc(userObjectId));
        } else {
            // 仅获取最近的10个订单，并按时间倒序排列
            return ResponseEntity.ok(repository.findTop10ByUserIdOrderByCreatedAtDesc(userObjectId));
        }
    }


    @PostMapping("/wxcreate")
    @LoginRequired
    @Operation(summary = "创建预支付订单。小程序先调用这个，再调用wx.requestPayment。response包含了调起支付所需的所有参数，可直接用于前端调起支付\n" +
            "<a href=https://github.com/wechatpay-apiv3/wechatpay-java/blob/main/service/src/example/java/com/wechat/pay/java/service/refund/RefundServiceExample.java>链接</a>")
    public PrepayWithRequestPaymentResponse create(@Valid @RequestBody OrderApiParams orderApiParams) throws Exception {
        securityService.checkIdImitate(orderApiParams.getUserId());
        Order order = orderService.buildOrderByApiParams(orderApiParams);
        order.setState(StateEnum.PRE_PAID.toString());
        Order save = repository.save(order);

        PrepayWithRequestPaymentResponse response = wechatPaymentService.jsapiTransaction(save);
        wechatPaymentService.closeIfNotPaid(save.getId().toHexString(), customRepository);
        return response;
    }


    /**
     * <a href="https://github.com/wechatpay-apiv3/wechatpay-java?tab=readme-ov-file#%E5%9B%9E%E8%B0%83%E9%80%9A%E7%9F%A5">链接</a>
     */
    @PostMapping("/wxcreate_callback")
    @Operation(summary = "接收支付结果通知。是腾讯的微信支付系统调用的")
    public ResponseEntity<String> callback(@RequestBody String requestBody) {
        try {
            NotificationParser parser = new NotificationParser(notificationConfig);
            RequestParam requestParam = buildRequestParam(requestBody);
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            String outTradeNo = transaction.getOutTradeNo();
            synchronized (outTradeNo.intern()) {//在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，以避免函数重入造成的数据混乱。
                //第一次调用状态机。发送NOTIFY_PRE_PAID事件
                Order order1 = repository.findById(new ObjectId(outTradeNo)).orElseThrow();
                if (!Objects.equals(order1.getState(), StateEnum.PRE_PAID.toString())) {//已处理，则直接返回结果成功
                    log.warn("[支付]微信重复发送通知给订单号为{}的订单。已处理，则直接返回结果成功", outTradeNo);
                    return ResponseEntity.ok("");
                }
                NotifyPrePayContext notifyPrePayContext = new NotifyPrePayContext();
                notifyPrePayContext.setTransaction(transaction);
                StateContext<NotifyPrePayContext> context = new StateContext<>(order1, notifyPrePayContext);
                ServiceResult<Object, NotifyPrePayContext> prePaidFSMResult = orderFsmEngine.sendEvent(EventEnum.NOTIFY_PRE_PAID.toString(), context);
                if (!prePaidFSMResult.isSuccess()) {
                    return ResponseEntity.internalServerError().body(
                            String.format("{\"code\":\"FAIL\",\"message\":\"%s\"}", prePaidFSMResult.getMsg())
                    );
                }
                if (Objects.equals(context.getOrderState(), StateEnum.ABNORMAL_END.toString())) {
                    return ResponseEntity.internalServerError().body(String.format("{\"code\":\"FAIL\",\"message\":\"%s\"}", "交易状态非‘支付成功’（建议重新下单），当前状态：" + transaction.getTradeState()));
                }

                //核销优惠
                if (order1.getCoupon() != null) {
                    String orderCouponUUID = order1.getCoupon().getUuid();
                    ObjectId userId = order1.getUserId();
                    benefitService.redeemUserCoupon(userId, orderCouponUUID);
                }

                //消费统计 与积分累计
                User user = userRepository.findById(order1.getUserId()).orElseThrow();
                user.setOrderNum(user.getOrderNum() + 1);
                user.setExpend(user.getExpend() + order1.getTotalPrice());

                user.setPoints(user.getPoints() + order1.getPoints());

                userRepository.save(user);

                //第二次调用状态机。从PAID状态转变
                Order order2 = repository.findById(new ObjectId(outTradeNo)).orElseThrow();
                ServiceResult<Object, ?> serviceResult = new ServiceResult<>();
                if (SceneEnum.FIX_DELIVERY.toString().equals(order2.getScene())) {
                    FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
                    StateContext<FixDeliveryContext> context2 = new StateContext<>(order2, fixDeliveryContext);
                    serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_FIX_DELIVERY.toString(), context2);
                } else if (SceneEnum.DINE_IN.toString().equals(order2.getScene())) {
                    DineInContext dineInContext = new DineInContext();
                    StateContext<DineInContext> context2 = new StateContext<>(order2, dineInContext);
                    serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_DINE_IN.toString(), context2);
                } else if (SceneEnum.TAKE_OUT.toString().equals(order2.getScene())) {
                    TakeOutContext takeOutContext = new TakeOutContext();
                    StateContext<TakeOutContext> context2 = new StateContext<>(order2, takeOutContext);
                    serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_TAKE_OUT.toString(), context2);
                }

                if (serviceResult.isSuccess()) {
                    return ResponseEntity.ok("");
                } else {
                    log.warn("竟然走到了这个分支！当支付成功之后状态机理应顺畅成功");
                    return ResponseEntity.internalServerError().body(
                            String.format("{\"code\":\"FAIL\",\"message\":\"%s\"}", serviceResult.getMsg())
                    );
                }
            }
        } catch (Exception e) {
            log.error("支付回调出现异常", e);
            return ResponseEntity.internalServerError().body(
                    String.format("{\"code\":\"FAIL\",\"message\":\"%s\"}", e.getMessage())
            );
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
    public ResponseEntity<ServiceResult> createOrderInStore(@Valid @RequestBody OrderApiParams orderApiParams) throws Exception {
        securityService.checkIdImitate(orderApiParams.getUserId());
        Order order = orderService.buildOrderByApiParamsShop(orderApiParams);
        order.setState(StateEnum.PRE_PAID.toString());
        // 保存订单
        Order order1 = repository.save(order);
        // FSM 状态机处理
        //1.预支付到支付状态，过一遍积分的逻辑
        NotifyPrePayContext notifyPrePayContext = new NotifyPrePayContext();
        StateContext<NotifyPrePayContext> context1 = new StateContext<>(order1, notifyPrePayContext);
        orderFsmEngine.sendEvent(EventEnum.NOTIFY_PRE_PAID.toString(), context1);

        //核销优惠  TODO:上线后可去掉
        if (order.getCoupon() != null) {
            String orderCouponUUID = order.getCoupon().getUuid();
            ObjectId userId = order.getUserId();
            benefitService.redeemUserCoupon(userId, orderCouponUUID);
        }

        //2.支付状态到其它别的状态
        Order order2 = repository.findById(order1.getId()).orElseThrow();
        ServiceResult<Object, ?> serviceResult = null;
        // 根据不同的场景设置上下文并发送事件
        if (SceneEnum.FIX_DELIVERY.toString().equals(order2.getScene())) {
            FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
            StateContext<FixDeliveryContext> context2 = new StateContext<>(order2, fixDeliveryContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_FIX_DELIVERY.toString(), context2);
        } else if (SceneEnum.DINE_IN.toString().equals(order2.getScene())) {
            DineInContext dineInContext = new DineInContext();
            StateContext<DineInContext> context2 = new StateContext<>(order2, dineInContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_DINE_IN.toString(), context2);
        } else if (SceneEnum.TAKE_OUT.toString().equals(order2.getScene())) {
            TakeOutContext takeOutContext = new TakeOutContext();
            StateContext<TakeOutContext> context2 = new StateContext<>(order2, takeOutContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.NEED_TAKE_OUT.toString(), context2);
        }

        // 根据状态机的处理结果返回不同的响应
        if (serviceResult != null && serviceResult.isSuccess()) {
            webSocketConfig.getOrdersWebSocketHandler().sendOrderWSDTO(order);
            return ResponseEntity.ok(serviceResult);
        } else {
            return ResponseEntity.internalServerError().body(serviceResult);
        }
    }


    @PostMapping(value = "/supply")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<ResponseBodyDTO<ServiceResult>> supplyOrder(@org.springframework.web.bind.annotation.RequestParam("orderId") String orderId) throws Exception {
        Optional<Order> optionalOrder = repository.findById(new ObjectId(orderId));
        ResponseBodyDTO<ServiceResult> dto = new ResponseBodyDTO<>();
        if (optionalOrder.isEmpty()) {
            dto.setMsg("根据id没找到相应的订单");
            return ResponseEntity.internalServerError().body(dto);
        }
        Order order = optionalOrder.get();
        ServiceResult<Object, ?> serviceResult = null;

        if (SceneEnum.FIX_DELIVERY.toString().equals(order.getScene())) {
            FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
            StateContext<FixDeliveryContext> context = new StateContext<>(order, fixDeliveryContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.SUPPLY_FIX_DELIVERY.toString(), context);
        } else if (SceneEnum.DINE_IN.toString().equals(order.getScene())) {
            DineInContext dineInContext = new DineInContext();
            StateContext<DineInContext> context = new StateContext<>(order, dineInContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.SUPPLY_DINE_IN.toString(), context);
        } else if (SceneEnum.TAKE_OUT.toString().equals(order.getScene())) {
            TakeOutContext takeOutContext = new TakeOutContext();
            StateContext<TakeOutContext> context = new StateContext<>(order, takeOutContext);
            serviceResult = orderFsmEngine.sendEvent(EventEnum.SUPPLY_TAKE_OUT.toString(), context);
        }

        if (serviceResult != null && serviceResult.isSuccess()) {
            dto.setMsg("已供餐");
            dto.setData(serviceResult);
            return ResponseEntity.ok(dto);
        } else {
            dto.setMsg("状态机出问题了");
            dto.setData(serviceResult);
            return ResponseEntity.internalServerError().body(dto);
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

                FixDeliveryContext fixDeliveryContext = new FixDeliveryContext();
                StateContext<FixDeliveryContext> context = new StateContext<>(order, fixDeliveryContext);
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

    @PostMapping(value = "/refund_apply")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<ResponseBodyDTO<ServiceResult>> refundApply(@Valid @RequestBody RefundApplyApiParams body) throws Exception {
        ResponseBodyDTO<ServiceResult> dto = new ResponseBodyDTO<>();

        String orderId = body.getOrderId();
        String reason = body.getReason();
        Optional<Order> orderOptional = repository.findById(new ObjectId(orderId));
        if (orderOptional.isEmpty()) {
            dto.setMsg("订单 " + orderId + " 未找到");
            return ResponseEntity.badRequest().body(dto);
        }
        Order order = orderOptional.get();
        RefundApplyContext refundApplyContext = new RefundApplyContext();
        refundApplyContext.setReason(reason);
        StateContext<RefundApplyContext> context = new StateContext<>(order, refundApplyContext);
        ServiceResult<Object, RefundApplyContext> result = orderFsmEngine.sendEvent(EventEnum.REFUND_APPLY.toString(), context);

        if (result != null && result.isSuccess()) {
            dto.setData(result);
            return ResponseEntity.ok(dto);
        } else {
            dto.setMsg("状态机出问题了");
            dto.setData(result);
            return ResponseEntity.internalServerError().body(dto);
        }
    }

    @PostMapping(value = "/refund_callback")
    @Operation(summary = "接收退款结果通知。是腾讯的微信支付系统调用的")
    public ResponseEntity<String> refundCallback(@RequestBody String body) {
        try {
            RequestParam requestParam = buildRequestParam(body);
            NotificationParser parser = new NotificationParser(notificationConfig);
            RefundNotification refundNotification = parser.parse(requestParam, RefundNotification.class);
            String outRefundNo = refundNotification.getOutRefundNo();
            synchronized (outRefundNo.intern()) {
                Order order = repository.findById(new ObjectId(outRefundNo)).orElseThrow();
                if (!Objects.equals(order.getState(), StateEnum.WAITING_REFUND_NOTIFICATION.toString())) {
                    log.warn("[退款]微信重复发送通知给订单号为{}的订单。已处理，则直接返回结果成功", outRefundNo);
                    return ResponseEntity.ok("");
                }
                StateContext<NotifyRefundResultContext> context = new StateContext<>(order, new NotifyRefundResultContext(refundNotification));
                ServiceResult<Object, NotifyRefundResultContext> result = orderFsmEngine.sendEvent(EventEnum.NOTIFY_REFUND_RESULT.toString(), context);
                if (Objects.equals(context.getOrderState(), StateEnum.ABNORMAL_END.toString())) {
                    return ResponseEntity.internalServerError().body(String.format("{\"code\":\"FAIL\",\"message\":\"%s\"}", "退款状态非‘退款成功’，当前状态：" + refundNotification.getRefundStatus()));
                }
                if (result != null && result.isSuccess()) {
                    return ResponseEntity.ok("");
                } else {
                    log.warn("[退款]竟然走到了这个分支！退款成功之后状态机理应顺畅成功");
                    return ResponseEntity.internalServerError().body(
                            String.format("{\"code\":\"FAIL\",\"message\":\"%s\"}", result != null ? result.getMsg() : "状态机结果为空")
                    );
                }

            }
        } catch (Exception e) {
            log.error("退款回调出现异常", e);
            return ResponseEntity.internalServerError().body(
                    String.format("{\"code\":\"FAIL\",\"message\":\"%s\"}", e.getMessage())
            );
        }
    }
}
