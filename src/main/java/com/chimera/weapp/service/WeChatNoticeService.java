package com.chimera.weapp.service;

import com.chimera.weapp.apiparams.SupplyNoticeApiParams;
import com.chimera.weapp.apiparams.RefundNoticeApiParams;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.AppConfigurationRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.util.DateUtil;
import com.chimera.weapp.util.DecimalUtil;
import com.chimera.weapp.vo.DeliveryInfo;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import static com.chimera.weapp.repository.AppConfigurationRepository.*;


@Service
@Slf4j
public class WeChatNoticeService {
    @Autowired
    private WeChatRequestService weChatRequestService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppConfigurationRepository acRepo;
    @Autowired
    private OrderService orderService;

    public static final String DINE_IN_OR_TAKE_OUT = "dineInOrTakeOut";
    public static final String FIX_DELIVERY = "fixDelivery";
    public static final String REFUND = "refund";


    public void dineInOrTakeOutNotice(Order order) {
        ObjectId userId = order.getUserId();
        int orderNum = order.getOrderNum();
        String openid = userRepository.findById(userId).orElseThrow().getOpenid();
        String page = acRepo.findByKeyAndCategory(PAGE, DINE_IN_OR_TAKE_OUT).orElseThrow().getValue();
        String shopAddress = acRepo.findByKey(SHOP_ADDRESS).orElseThrow().getValue();
        String phoneNumber = acRepo.findByKey(CONTACT_PHONE_NUMBER).orElseThrow().getValue();
        String wxts = acRepo.findByKeyAndCategory(WXTS, DINE_IN_OR_TAKE_OUT).orElseThrow().getValue();

        String templateId = acRepo.findByKeyAndCategory(TEMPLATE_ID, DINE_IN_OR_TAKE_OUT).orElseThrow().getValue();
        SupplyNoticeApiParams.SupplyNoticeApiParamsBuilder builder = SupplyNoticeApiParams.builder()
                .character_string19(Integer.toString(orderNum))
                .thing11(orderService.getDescription(order))
                .thing27(shopAddress)
                .phone_number32(phoneNumber)
                .thing7(wxts);
        try {
            weChatRequestService.subscribeSend(builder.build().buildGouCaoData(), page, templateId, openid);
        } catch (URISyntaxException | IOException e) {
            log.error("发送通知[提醒顾客来取餐]失败", e);
            throw new RuntimeException(e);
        }
    }

    public void fixDeliveryNotice(Order order) {
        ObjectId userId = order.getUserId();
        String openid = userRepository.findById(userId).orElseThrow().getOpenid();
        int orderNum = order.getOrderNum();
        String wxts = acRepo.findByKeyAndCategory(WXTS, FIX_DELIVERY).orElseThrow().getValue();
        String page = acRepo.findByKeyAndCategory(PAGE, FIX_DELIVERY).orElseThrow().getValue();
        String templateId = acRepo.findByKeyAndCategory(TEMPLATE_ID, FIX_DELIVERY).orElseThrow().getValue();
        String phoneNumber = acRepo.findByKey(CONTACT_PHONE_NUMBER).orElseThrow().getValue();
        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        SupplyNoticeApiParams.SupplyNoticeApiParamsBuilder builder = SupplyNoticeApiParams.builder()
                .character_string19(Integer.toString(orderNum))
                .thing11(orderService.getDescription(order))
                .thing27(deliveryInfo.getSchool() + " " + deliveryInfo.getAddress())
                .phone_number32(phoneNumber)
                .thing7(wxts);
        try {
            log.info("准备发送订阅消息[提醒]，订单号{}", order.getUserId());
            weChatRequestService.subscribeSend(builder.build().buildGouCaoData(), page, templateId, openid);
        } catch (URISyntaxException | IOException e) {
            log.error("发送订阅消息[提醒]失败", e);
            throw new RuntimeException(e);
        }
    }

    public void refundNotice(Order order) {
        String reason = order.getRefundReason();
        ObjectId userId = order.getUserId();
        String openid = userRepository.findById(userId).orElseThrow().getOpenid();
        String wxts = acRepo.findByKeyAndCategory(WXTS, REFUND).orElseThrow().getValue();
        String page = acRepo.findByKeyAndCategory(PAGE, REFUND).orElseThrow().getValue();
        String templateId = acRepo.findByKeyAndCategory(TEMPLATE_ID, REFUND).orElseThrow().getValue();
        RefundNoticeApiParams.RefundNoticeApiParamsBuilder builder = RefundNoticeApiParams.builder()
                .number1(Integer.toString(order.getOrderNum()))
                .amount3(DecimalUtil.convertYuanToFen(Integer.toString(order.getTotalPrice())) + "元")
                .time2(DateUtil.formatDate(new Date()))
                .thing4(reason)
                .thing8(wxts);
        try {
            log.info("准备发送订阅消息[退款]，订单号{}", order.getId());
            weChatRequestService.subscribeSend(builder.build().buildGouCaoData(), page, templateId, openid);
        } catch (URISyntaxException | IOException e) {
            log.error("发送订阅消息[退款]失败", e);
            throw new RuntimeException(e);
        }
    }

}
