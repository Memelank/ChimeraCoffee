package com.chimera.weapp.service;

import com.chimera.weapp.apiparams.DineInOrTakeOutApiParams;
import com.chimera.weapp.apiparams.FixDeliveryApiParams;
import com.chimera.weapp.entity.AppConfiguration;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.AppConfigurationRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;

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
    private AppConfigurationRepository appConfigurationRepository;
    @Value("${wx-mini-program.noticeInfo.dineInOrTakeOut.templateID}")
    private String templateId_DT;
    @Value("${wx-mini-program.noticeInfo.fixDelivery.templateID}")
    private String templateId_FD;
    @Value("${wx-mini-program.noticeInfo.fixDelivery.wxts}")
    private String wxts_FD;
    @Value("${wx-mini-program.noticeInfo.dineInOrTakeOut.wxts}")
    private String wxts_DT;
    @Value("${wx-mini-program.noticeInfo.shopName}")
    private String shopName;
    @Value("${wx-mini-program.noticeInfo.address}")
    private String address;
    @Value("${wx-mini-program.noticeInfo.dineInOrTakeOut.page}")
    private String page;

    private static final String contact_phone_number = "contact_phone_number";

    public void dineInOrTakeOutNotice(String orderId, String orderState) {
        Order order = orderRepository.findById(new ObjectId(orderId)).orElseThrow();
        ObjectId userId = order.getUserId();
        String openid = userRepository.findById(userId).orElseThrow().getOpenid();
        int orderNum = order.getOrderNum();
        DineInOrTakeOutApiParams.DineInOrTakeOutApiParamsBuilder builder = DineInOrTakeOutApiParams.builder()
                .phrase19(orderState)
                .thing11(wxts_DT)
                .thing2(shopName)
                .thing7(address)
                .character_string4(Integer.toString(orderNum));
        try {
            weChatRequestService.subscribeSend(builder.build(), page, templateId_DT, openid);
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
        AppConfiguration configuration = appConfigurationRepository.findByKey(contact_phone_number).orElseThrow();
        String phoneNumber = configuration.getValue();
        FixDeliveryApiParams.FixDeliveryApiParamsBuilder builder = FixDeliveryApiParams.builder()
                .time9(time)
                .thing17(deliveryAddress)
                .phone_number16(phoneNumber)
                .thing5(wxts_FD)
                .character_string1(Integer.toString(orderNum));
        try {
            weChatRequestService.subscribeSend(builder.build(), page, templateId_FD, openid);
        } catch (URISyntaxException | IOException e) {
            log.error("发送通知[提醒顾客定时达消息]失败", e);
            throw new RuntimeException(e);
        }
    }

}
