package com.chimera.weapp.service;

import com.chimera.weapp.apiparams.OrderApiParams;
import com.chimera.weapp.apiparams.OrderItemApiParams;
import com.chimera.weapp.dto.UserDTO;
import com.chimera.weapp.entity.*;
import com.chimera.weapp.repository.*;
import com.chimera.weapp.util.ThreadLocalUtil;
import com.chimera.weapp.vo.CouponIns;
import com.chimera.weapp.vo.OptionValue;
import com.chimera.weapp.vo.OrderItem;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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
    private CouponRepository couponRepository;

    public Order buildOrderByApiParams(OrderApiParams orderApiParams) {
        List<OrderItem> orderItems = buildItemsByApiParams(orderApiParams.getItems());
        Order.OrderBuilder orderBuilder = Order.builder().userId(orderApiParams.getUserId())
                .customerType(orderApiParams.getCustomerType())
                .scene(orderApiParams.getScene())
                .deliveryInfo(orderApiParams.getDeliveryInfo())
                .items(orderItems)
                .remark(orderApiParams.getRemark())
                .merchantNote(orderApiParams.getMerchantNote());

        int orderItemPriceSum = orderItems.stream().map(OrderItem::getPrice).reduce(Integer::sum).orElseThrow();
        UserDTO userDTO = ThreadLocalUtil.get(ThreadLocalUtil.USER_DTO, UserDTO.class);
        if (!Objects.isNull(orderApiParams.getCouponInsUUID())) {
            CouponIns couponIns = getCouponInsFromUserByUUID(userDTO.getId(), orderApiParams.getCouponInsUUID(),
                    orderApiParams.getItems().stream().map(OrderItemApiParams::getProductId).toList());
            orderBuilder.coupon(couponIns);
            orderBuilder.totalPrice(orderItemPriceSum - couponIns.getDePrice());
        }
        orderBuilder.totalPrice(orderItemPriceSum);

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
                String couponId = couponIns.getCouponId();
                Coupon coupon = couponRepository.findById(new ObjectId(couponId)).orElseThrow();
                if (Objects.isNull(coupon.getCateId())) {
                    return couponIns;
                }
                for (ObjectId productId : productIds) {
                    Product product = productRepository.findById(productId).orElseThrow();
                    if (Objects.equals(product.getCateId(), coupon.getCateId())) {
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
                        map.put(optionId,actualOptionValue);
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

    //订单金额由OrderItem组成
    //校验每个OrderItem的金额是对的
    //OrderItem的金额由product的基础金额+option金额决定
    //具体校验的是
    //option金额和数据库的对得上
    //OrderItem的price=product基础金额+option金额
    //OrderItem的price总额=totalPrice
    public void checkPrice(Order order) {
        List<OrderItem> orderItems = order.getItems();
        int actualTotalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            int actualOrderItemPrice = 0;
            ObjectId productId = orderItem.getProductId();
            Product product = productRepository.findById(productId).orElseThrow();
            actualOrderItemPrice += product.getPrice();
            Map<String, OptionValue> stringOptionValueMap = orderItem.getOptionValues();
            for (Map.Entry<String, OptionValue> entry : stringOptionValueMap.entrySet()) {
                String optionId = entry.getKey();
                OptionValue optionValue = entry.getValue();
                ProductOption actualProductOption = productOptionRepository.findById(new ObjectId(optionId)).orElseThrow();
                boolean match = false;
                for (OptionValue actualOptionValue : actualProductOption.getValues()) {
                    if (Objects.equals(actualOptionValue.getUuid(), optionValue.getUuid())
                            && Objects.equals(actualOptionValue.getPriceAdjustment(), optionValue.getPriceAdjustment())) {
                        match = true;
                        actualOrderItemPrice += optionValue.getPriceAdjustment();
                        break;
                    }
                }
                if (!match) {
                    throw new RuntimeException("输入价格与实际价格不匹配");
                }
            }
            //检查OrderItem的price=product基础金额+option金额
            if (actualOrderItemPrice != orderItem.getPrice()) {
                throw new RuntimeException("输入价格与实际价格不匹配");
            } else {
                actualTotalPrice += actualOrderItemPrice;
            }
        }
        //OrderItem的price总额=totalPrice
        if (actualTotalPrice != order.getTotalPrice()) {
            throw new RuntimeException("输入价格与实际价格不匹配");
        }
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
        String str = String.join(",", array);
        return str + "\n" + order.getScene();
    }
}
