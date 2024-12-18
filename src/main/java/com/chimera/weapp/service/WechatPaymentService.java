package com.chimera.weapp.service;

import com.chimera.weapp.dto.UserDTO;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.util.ThreadLocalUtil;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class WechatPaymentService {
    @Autowired
    private OrderService orderService;
    @Value("${wx-mini-program.appid}")
    private String appid;
    @Value("${wx-mini-program.mchid}")
    private String mchid;
    @Value("${wx-mini-program.prepay.notify_url}")
    private String prepayNotifyURL;
    @Value("${wx-mini-program.refund.notify_url}")
    private String refundNotifyURL;
    private Config config;
    private final JsapiService jsapiService;

    public WechatPaymentService(Config config) {
        jsapiService = new JsapiService.Builder().config(config).build();
        this.config = config;
    }

    /**
     * JSAPI 支付和 APP 支付推荐使用服务拓展类 JsapiServiceExtension 和 AppServiceExtension，两者包含了下单并返回调起支付参数方法。
     * <a href="https://github.com/wechatpay-apiv3/wechatpay-java/tree/main?tab=readme-ov-file#%E4%B8%8B%E5%8D%95%E5%B9%B6%E7%94%9F%E6%88%90%E8%B0%83%E8%B5%B7%E6%94%AF%E4%BB%98%E7%9A%84%E5%8F%82%E6%95%B0">链接</a>
     */
    public PrepayWithRequestPaymentResponse jsapiTransaction(Order order) {

        JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(config).build();
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(order.getTotalPrice());
        Payer payer = new Payer();
        UserDTO userDTO = ThreadLocalUtil.get(ThreadLocalUtil.USER_DTO);
        payer.setOpenid(userDTO.getOpenid());
        request.setPayer(payer);
        request.setAmount(amount);
        request.setAppid(appid);
        request.setMchid(mchid);
        request.setDescription(orderService.getDescription(order));
        request.setNotifyUrl(prepayNotifyURL);
        request.setOutTradeNo(order.getId().toHexString());
        return service.prepayWithRequestPayment(request);
    }

    public void closeIfNotPaid(String orderId, CustomRepository customRepository) {
        // 查询间隔（以毫秒为单位）
        int[] intervals = {5000, 30000, 60000, 180000, 300000, 600000, 1800000};//5 30 60 180 300 600 1800 总共2975s约50min
        new Thread(() -> {
            for (int interval : intervals) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                boolean result = callWeChatPayQueryAPI(orderId); // 调用查询接口
                if (result) {
                    return;
                }
            }
            log.info("对{}调用关单接口", orderId);
            callWeChatPayCloseAPI(orderId); // 调用关单接口
            customRepository.updateOrderStateById(orderId, StateEnum.ORDER_CLOSED.toString());
        }).start();
    }

    private boolean callWeChatPayQueryAPI(String orderId) {
        QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
        queryRequest.setMchid(mchid);
        queryRequest.setOutTradeNo(orderId);
        Transaction transaction = jsapiService.queryOrderByOutTradeNo(queryRequest);
        return Objects.equals(transaction.getTradeState(), Transaction.TradeStateEnum.SUCCESS);
    }

    private void callWeChatPayCloseAPI(String orderId) {
        CloseOrderRequest closeRequest = new CloseOrderRequest();
        closeRequest.setMchid(mchid);
        closeRequest.setOutTradeNo(orderId);
        jsapiService.closeOrder(closeRequest);
    }

    /**
     * 退款申请
     * <a href="https://github.com/wechatpay-apiv3/wechatpay-java/blob/main/service/src/example/java/com/wechat/pay/java/service/refund/RefundServiceExample.java">链接</a>
     */
    public Refund createRefund(Order order, String reason) {
        RefundService service = new RefundService.Builder().config(config).build();
        CreateRequest createRequest = new CreateRequest();
        createRequest.setOutTradeNo(order.getId().toHexString());
        createRequest.setOutRefundNo(order.getId().toHexString());
        createRequest.setReason(reason);
        createRequest.setNotifyUrl(refundNotifyURL);
        AmountReq amountReq = new AmountReq();
        amountReq.setRefund(Integer.toUnsignedLong(order.getTotalPrice()));
        amountReq.setTotal(Integer.toUnsignedLong(order.getTotalPrice()));// 当前退款金额即原订单金额
        amountReq.setCurrency("CNY");
        createRequest.setAmount(amountReq);
        return service.create(createRequest);
    }
}
