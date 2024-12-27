package com.chimera.weapp.service;

import com.chimera.weapp.apiparams.OrderApiParams;
import com.chimera.weapp.apiparams.OrderItemApiParams;
import com.chimera.weapp.dto.UserDTO;
import com.chimera.weapp.entity.*;
import com.chimera.weapp.repository.*;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.engine.OrderFsmEngine;
import com.chimera.weapp.statemachine.enums.ErrorCodeEnum;
import com.chimera.weapp.statemachine.exception.FsmException;
import com.chimera.weapp.util.ThreadLocalUtil;
import com.chimera.weapp.util.TimeRangeChecker;
import com.chimera.weapp.vo.CouponIns;
import com.chimera.weapp.vo.OptionValue;
import com.chimera.weapp.vo.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.List;

import static com.chimera.weapp.repository.AppConfigurationRepository.*;

@Slf4j
@Component
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductOptionRepository productOptionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderFsmEngine orderFsmEngine;
    @Autowired
    private AppConfigurationRepository appConfigurationRepository;

    public Order buildOrderByApiParams(OrderApiParams orderApiParams) throws Exception {
        List<OrderItem> orderItems = buildItemsByApiParams(orderApiParams.getItems());
        Order.OrderBuilder orderBuilder = Order.builder().userId(orderApiParams.getUserId())
                .customerType(orderApiParams.getCustomerType())
                .scene(orderApiParams.getScene())
                .deliveryInfo(orderApiParams.getDeliveryInfo())
                .items(orderItems)
                .remark(orderApiParams.getRemark())
                .merchantNote(orderApiParams.getMerchantNote());

        // 如果 DeliveryInfo 不为空，更新用户的电话号码
        if (orderApiParams.getDeliveryInfo() != null && orderApiParams.getDeliveryInfo().getNumber() != null) {
            User user = userRepository.findById(orderApiParams.getUserId()).orElse(null);
            if (user != null) {
                user.setNumber(orderApiParams.getDeliveryInfo().getNumber());
                userRepository.save(user);
            }
        }

        int orderItemPriceSum = orderItems.stream().map(OrderItem::getPrice).reduce(Integer::sum).orElseThrow();
        UserDTO userDTO = ThreadLocalUtil.get(ThreadLocalUtil.USER_DTO);
        if (!Objects.isNull(orderApiParams.getCouponInsUUID())) {
            CouponIns couponIns = getCouponInsFromUserByUUID(userDTO.getId(), orderApiParams.getCouponInsUUID(),
                    orderApiParams.getItems().stream().map(OrderItemApiParams::getProductId).toList());

            if (couponIns.getStatus() != 0) {
                throw new Exception("Coupon has already been used");
            }

            orderBuilder.coupon(couponIns);
            orderBuilder.totalPrice(orderItemPriceSum - couponIns.getDePrice());
        } else {
            orderBuilder.totalPrice(orderItemPriceSum);
        }

        // 获取当前日期的开始时间（0点）
        Date startOfDay = getStartOfDay(new Date());
        // 查询当天的订单数量
        long orderCountToday = orderRepository.countByCreatedAtGreaterThanEqual(startOfDay);
        // 设置订单号，从1开始累计
        orderBuilder.orderNum((int) orderCountToday + 1);
        return orderBuilder.build();
    }

    public Order buildOrderByApiParamsShop(OrderApiParams orderApiParams) {
        List<OrderItem> orderItems = buildItemsByApiParams(orderApiParams.getItems());
        Order.OrderBuilder orderBuilder = Order.builder().userId(orderApiParams.getUserId())
                .customerType(orderApiParams.getCustomerType())
                .scene(orderApiParams.getScene())
                .deliveryInfo(orderApiParams.getDeliveryInfo())
                .items(orderItems)
                .remark(orderApiParams.getRemark())
                .merchantNote(orderApiParams.getMerchantNote())
                .disPrice(orderApiParams.getDisPrice());

        int orderItemPriceSum = orderItems.stream().map(OrderItem::getPrice).reduce(Integer::sum).orElseThrow();
        UserDTO userDTO = ThreadLocalUtil.get(ThreadLocalUtil.USER_DTO);
        if (!Objects.isNull(orderApiParams.getCouponInsUUID())) {
            CouponIns couponIns = getCouponInsFromUserByUUID(userDTO.getId(), orderApiParams.getCouponInsUUID(),
                    orderApiParams.getItems().stream().map(OrderItemApiParams::getProductId).toList());
            orderBuilder.coupon(couponIns);
            orderBuilder.totalPrice(orderItemPriceSum - couponIns.getDePrice());
        }
        orderBuilder.totalPrice(orderItemPriceSum - orderApiParams.getDisPrice());

        // 获取当前日期的开始时间（0点）
        Date startOfDay = getStartOfDay(new Date());
        // 查询当天的订单数量
        long orderCountToday = orderRepository.countByCreatedAtGreaterThanEqual(startOfDay);
        // 设置订单号，从1开始累计
        orderBuilder.orderNum((int) orderCountToday + 1);
        return orderBuilder.build();
    }

    public CouponIns getCouponInsFromUserByUUID(String userId, String couponInsUUIDInput, List<ObjectId> productIds) {
        User user = userRepository.findById(new ObjectId(userId)).orElseThrow();
        List<CouponIns> coupons = user.getCoupons();
        for (CouponIns couponIns : coupons) {
            String couponInsUUID = couponIns.getUuid();
            if (Objects.equals(couponInsUUID, couponInsUUIDInput)) {
                String cateId = couponIns.getCateId();
                if (Objects.isNull(cateId)) {
                    return couponIns;
                }
                for (ObjectId productId : productIds) {
                    Product product = productRepository.findById(productId).orElseThrow();
                    if (Objects.equals(product.getCateId().toHexString(), cateId)) {
                        return couponIns;
                    }
                }
            }
        }
        throw new RuntimeException("当前用户没有对应的优惠券或优惠券不适用于本订单中的任何一个商品");
    }

    public List<OrderItem> buildItemsByApiParams(List<OrderItemApiParams> items) {
        ArrayList<OrderItem> res = new ArrayList<>();
        for (OrderItemApiParams orderItemApiParams : items) {
            ObjectId productId = orderItemApiParams.getProductId();
            Product product = productRepository.findById(productId).orElseThrow();
            String name = product.getName();
            ObjectId cateId = product.getCateId();
            int actualOrderItemPrice = 0;
            actualOrderItemPrice += product.getPrice();
            Map<String, String> optionValues = orderItemApiParams.getOptionValues();
            HashMap<String, OptionValue> map = new HashMap<>();
            for (Map.Entry<String, String> entry : optionValues.entrySet()) {
                String optionId = entry.getKey();
                String optionValueUUID = entry.getValue();
                ProductOption actualProductOption = productOptionRepository.findById(new ObjectId(optionId)).orElseThrow();
                for (OptionValue actualOptionValue : actualProductOption.getValues()) {
                    if (Objects.equals(actualOptionValue.getUuid(), optionValueUUID)) {
                        map.put(optionId, actualOptionValue);
                        actualOrderItemPrice += actualOptionValue.getPriceAdjustment();
                        break;
                    }
                }
            }
            OrderItem orderItem = OrderItem.builder().productId(productId)
                    .optionValues(map)
                    .name(name)
                    .cateId(cateId)
                    .imgURL(product.getImgURL())
                    .price(actualOrderItemPrice).build();
            res.add(orderItem);
        }
        return res;
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


    public String getDescription(Order order) {
        TreeMap<String, Integer> treeMap = new TreeMap<>();

        for (OrderItem item : order.getItems()) {
            ObjectId productId = item.getProductId();
            Product product = productRepository.findById(productId).orElseThrow();
            String productName = product.getName();
            treeMap.put(productName, treeMap.getOrDefault(productName, 0) + 1);
        }
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : treeMap.entrySet()) {
            String name = entry.getKey();
            Integer count = entry.getValue();
            String str = name + "×" + count;
            list.add(str);
        }
        String[] array = list.toArray(new String[0]);
        return String.join(",", array);
    }

    public void sendAnEventAfterACertainPeriodOfTime(ObjectId orderId, String event) {
        AppConfiguration time = appConfigurationRepository.findByKey(THE_PERIOD_OF_TIME).orElseThrow();
        String timeValue = time.getValue();
        if (checkCondition()) {
            new Thread(() -> {
                try {
                    Order order = orderRepository.findById(orderId).orElseThrow();
                    Thread.sleep(Long.parseLong(timeValue));
                    orderFsmEngine.sendEvent(event, new StateContext<>(order, new StateContext<>()));
                } catch (FsmException e) {
                    if (Objects.equals(e.getMessage(), ErrorCodeEnum.NOT_FOUND_PROCESSOR.toString()) ||
                            Objects.equals(e.getMessage(), ErrorCodeEnum.FILTER_NOT_FOUND_PROCESSOR.toString()) ||
                            Objects.equals(e.getMessage(), ErrorCodeEnum.FOUND_MORE_PROCESSOR.toString())) {
                        log.info("定时发送事件失败，因为没到对应的状态机,id{}", orderId.toHexString(), e);
                    } else {
                        log.error("定时发送事件失败，订单id为{}", orderId.toHexString());
                        throw new RuntimeException(e);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }


    private boolean checkCondition() {
        AppConfiguration switch_ = appConfigurationRepository.findByKey(PERIODICALLY_SEND_EVENT_SWITCH).orElseThrow();
        AppConfiguration start = appConfigurationRepository.findByKey(PERIODICALLY_SEND_EVENT_START_TIME).orElseThrow();
        AppConfiguration end = appConfigurationRepository.findByKey(PERIODICALLY_SEND_EVENT_END_TIME).orElseThrow();
        String switchValue = switch_.getValue();
        String startValue = start.getValue();
        String endValue = end.getValue();
        boolean open = Objects.equals(switchValue, "T");
        return open && TimeRangeChecker.isNowInTimeRange(startValue, endValue);
    }
}
