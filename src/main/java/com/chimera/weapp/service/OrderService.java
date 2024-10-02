package com.chimera.weapp.service;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.entity.Product;
import com.chimera.weapp.entity.ProductOption;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.ProductOptionRepository;
import com.chimera.weapp.repository.ProductRepository;
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
