package com.chimera.weapp.service;

import com.chimera.weapp.apiparams.DineInOrTakeOutApiParams;
import com.chimera.weapp.apiparams.FixDeliveryApiParams;
import com.chimera.weapp.apiparams.RefundApiParams;
import com.chimera.weapp.entity.AppConfiguration;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.AppConfigurationRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.util.DateUtil;
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
    public static final String DINE_IN_OR_TAKE_OUT = "dineInOrTakeOut";
    public static final String FIX_DELIVERY = "fixDelivery";
    public static final String REFUND = "refund";


    public void dineInOrTakeOutNotice(String orderId, String orderState) {
        Order order = orderRepository.findById(new ObjectId(orderId)).orElseThrow();
        ObjectId userId = order.getUserId();
        String openid = userRepository.findById(userId).orElseThrow().getOpenid();
        int orderNum = order.getOrderNum();
        String wxts = acRepo.findByKeyAndCategory(WXTS, DINE_IN_OR_TAKE_OUT).orElseThrow().getValue();
        String shopName = acRepo.findByKey(SHOP_NAME).orElseThrow().getValue();
        String address = acRepo.findByKey(ADDRESS).orElseThrow().getValue();
        String page = acRepo.findByKeyAndCategory(PAGE, DINE_IN_OR_TAKE_OUT).orElseThrow().getValue();
        String templateId = acRepo.findByKeyAndCategory(TEMPLATE_ID, DINE_IN_OR_TAKE_OUT).orElseThrow().getValue();
        DineInOrTakeOutApiParams.DineInOrTakeOutApiParamsBuilder builder = DineInOrTakeOutApiParams.builder()
                .phrase19(orderState)
                .thing11(wxts)
                .thing2(shopName)
                .thing7(address)
                .character_string4(Integer.toString(orderNum));
        try {
            weChatRequestService.subscribeSend(builder.build(), page, templateId, openid);
        } catch (URISyntaxException | IOException e) {
            log.error("发送通知[提醒顾客来取餐]失败", e);
            throw new RuntimeException(e);
        }
    }

    public void fixDeliveryNotice(String orderId, String time, String deliveryAddress) {
        Order order = orderRepository.findById(new ObjectId(orderId)).orElseThrow();
        ObjectId userId = order.getUserId();
        String openid = userRepository.findById(userId).orElseThrow().getOpenid();
        int orderNum = order.getOrderNum();
        String wxts = acRepo.findByKeyAndCategory(WXTS, FIX_DELIVERY).orElseThrow().getValue();
        String page = acRepo.findByKeyAndCategory(PAGE, FIX_DELIVERY).orElseThrow().getValue();
        String templateId = acRepo.findByKeyAndCategory(TEMPLATE_ID, FIX_DELIVERY).orElseThrow().getValue();
        String phoneNumber = acRepo.findByKey(CONTACT_PHONE_NUMBER).orElseThrow().getValue();
        FixDeliveryApiParams.FixDeliveryApiParamsBuilder builder = FixDeliveryApiParams.builder()
                .time9(time)
                .thing17(deliveryAddress)
                .phone_number16(phoneNumber)
                .thing5(wxts)
                .character_string1(Integer.toString(orderNum));
        try {
            weChatRequestService.subscribeSend(builder.build(), page, templateId, openid);
        } catch (URISyntaxException | IOException e) {
            log.error("发送通知[提醒顾客定时达消息]失败", e);
            throw new RuntimeException(e);
        }
    }

    public void refundNotice(String orderId, String reason) {
        Order order = orderRepository.findById(new ObjectId(orderId)).orElseThrow();
        ObjectId userId = order.getUserId();
        String openid = userRepository.findById(userId).orElseThrow().getOpenid();
        String wxts = acRepo.findByKeyAndCategory(WXTS, REFUND).orElseThrow().getValue();
        String page = acRepo.findByKeyAndCategory(PAGE, REFUND).orElseThrow().getValue();
        String templateId = acRepo.findByKeyAndCategory(TEMPLATE_ID, REFUND).orElseThrow().getValue();
        RefundApiParams.RefundApiParamsBuilder builder = RefundApiParams.builder()
                .character_string1(Integer.toString(order.getOrderNum()))
                .amount2(Integer.toString(order.getTotalPrice()))
                .time3(DateUtil.formatDate(new Date()))
                .thing4(reason)
                .thing5(wxts);
        try {
            weChatRequestService.subscribeSend(builder.build(), page, templateId, openid);
        } catch (URISyntaxException | IOException e) {
            log.error("发送通知[提醒顾客定时达消息]失败", e);
            throw new RuntimeException(e);
        }
    }

}
