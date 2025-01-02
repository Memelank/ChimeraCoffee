package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.config.WebSocketConfig;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.entity.Product;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.ProductRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.service.BenefitService;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.NotifyPrePayContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import com.chimera.weapp.vo.OptionValue;
import com.chimera.weapp.vo.OrderItem;
import com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Processor(processorId = 0)
@Component
public class NotifyPrePay extends AbstractStateProcessor<String, NotifyPrePayContext> {
    @Autowired
    private CustomRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WebSocketConfig webSocketConfig;

    @Autowired
    private BenefitService benefitService;


    @Override
    public boolean filter(StateContext<NotifyPrePayContext> context) {
        return true;
    }


    @Override
    public ServiceResult<String, NotifyPrePayContext> actionStep(StateContext<NotifyPrePayContext> context) throws Exception {
        if (payIsNotSucceed(context)) {
            ServiceResult<String, NotifyPrePayContext> emptySuccess = new ServiceResult<>();
            emptySuccess.setSuccess(true);
            return emptySuccess;// 因为要让走到持久化那一步，且不更改用户的积分
        }

        User user = userRepository.findById(new ObjectId(context.getUserId())).orElseThrow();
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();

        //核销优惠
        if (order.getCoupon() != null) {
            String orderCouponUUID = order.getCoupon().getUuid();
            ObjectId userId = order.getUserId();
            benefitService.redeemUserCoupon(userId, orderCouponUUID);
        }

        //消费统计 与积分累计
        user.setOrderNum(user.getOrderNum() + 1);
        user.setExpend(user.getExpend() + order.getTotalPrice());
        user.setPoints(user.getPoints() + order.getPoints());

        userRepository.save(user);

        //购买统计
        Map<ObjectId, Integer> productsToUpdate = new HashMap<>();

        // 定义 OptionValue.value 到整数的映射
        Map<String, Integer> optionValueMap = new HashMap<>();
        optionValueMap.put("一个", 1);
        optionValueMap.put("两个", 2);
        optionValueMap.put("三个", 3);

        for (OrderItem item : order.getItems()) {
            // 获取对应的 Product
            Product product = productRepository.findById(item.getProductId()).orElseThrow(() ->
                    new IllegalArgumentException("Product not found for ID: " + item.getProductId())
            );

            if (Boolean.TRUE.equals(product.getNeedStockWithRestrictBuy())) {
                // 假设每个 OrderItem 的 optionValues 只有一个键值对
                if (item.getOptionValues() != null && item.getOptionValues().size() == 1) {
                    OptionValue optionValue = item.getOptionValues().values().iterator().next();
                    String valueStr = optionValue.getValue();
                    Integer quantity = optionValueMap.get(valueStr);
                    if (quantity != null) {
                        productsToUpdate.put(product.getId(), quantity);
                    } else {
                        throw new IllegalArgumentException("Unknown OptionValue: " + valueStr);
                    }
                } else {
                    throw new IllegalArgumentException("OrderItem optionValues is invalid for OrderItem: " + item.getProductId());
                }
            }
        }

        if (!productsToUpdate.isEmpty()) {
            boolean isTimedDelivery = "定时达".equals(order.getScene());
            boolean isTomorrow = false;

            if (isTimedDelivery && order.getDeliveryInfo() != null && order.getDeliveryInfo().getTime() != null) {
                // 获取订单的配送时间
                Instant deliveryInstant = order.getDeliveryInfo().getTime().toInstant();
                LocalDate deliveryDate = LocalDate.ofInstant(deliveryInstant, ZoneId.systemDefault());

                // 获取明天的日期
                LocalDate tomorrow = LocalDate.now().plusDays(1);

                isTomorrow = deliveryDate.equals(tomorrow);
            }

            for (Map.Entry<ObjectId, Integer> entry : productsToUpdate.entrySet()) {
                ObjectId productId = entry.getKey();
                int quantity = entry.getValue();

                Product product = productRepository.findById(productId).orElseThrow(() ->
                        new IllegalArgumentException("Product not found for ID: " + productId)
                );

                if (isTimedDelivery && isTomorrow) {
                    // 逻辑 A
                    if (Boolean.FALSE.equals(product.getStocked())) {
                        product.setPresaleNum(product.getPresaleNum() + quantity);
                    } else if (Boolean.TRUE.equals(product.getStocked())) {
                        if (product.getStock() < quantity) {
                            throw new IllegalArgumentException("Insufficient stock for Product ID: " + productId);
                        }
                        product.setStock(product.getStock() - quantity);
                    }
                } else {
                    // 逻辑 B
                    if (product.getStock() < quantity) {
                        throw new IllegalArgumentException("Insufficient stock for Product ID: " + productId);
                    }
                    product.setStock(product.getStock() - quantity);
                }

                productRepository.save(product);
            }
        }

        ServiceResult<String, NotifyPrePayContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public StateEnum getNextState(StateContext<NotifyPrePayContext> context) {
        if (payIsNotSucceed(context)) {
            log.warn("微信系统返回的支付通知显示结果并非成功,当前还不能处理这种情况需要客户和店员另想办法了，context:{}", context);
            return StateEnum.ABNORMAL_END;
        }
        return StateEnum.PAID;

    }

    @Override
    public ServiceResult<String, NotifyPrePayContext> save(String nextState, StateContext<NotifyPrePayContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "", true);
    }

    @Override
    public void after(StateContext<NotifyPrePayContext> context) {
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
        webSocketConfig.getOrdersWebSocketHandler().sendOrderWSDTO(order);
    }

    private boolean payIsNotSucceed(StateContext<NotifyPrePayContext> context) {
        Transaction transaction = context.getContext().getTransaction();
        if (transaction != null) { //兼容商户端下单，商户端下单不检查transaction
            Transaction.TradeStateEnum tradeState = transaction.getTradeState();
            return !Objects.equals(tradeState, Transaction.TradeStateEnum.SUCCESS);
        }
        return false;
    }
}
