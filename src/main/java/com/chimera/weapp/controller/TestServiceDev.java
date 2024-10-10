package com.chimera.weapp.controller;

import com.chimera.weapp.entity.User;
import com.chimera.weapp.entity.ProcessorMap;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.vo.OptionValue;
import com.chimera.weapp.vo.OrderItem;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;

import java.util.*;

import com.chimera.weapp.repository.ProcessorMapRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.UserRepository;

@Service
public class TestServiceDev {

    @Autowired
    private ProcessorMapRepository processorMapRepository;  // 假设你有一个 UserRepository

    public void createNewProcessorMap() {

        ProcessorMap processorMap = ProcessorMap.builder()
                .id(new ObjectId())  // 自动生成的ObjectId
                .state("预支付")  // 设置默认状态为NEW
                .event("支付成功")  // 设置默认事件
                .customerTypes(new String[]{"北大学生业务", "清华学生业务", "未认证为学生身份的用户业务"})  // 样例客户类型
                .scenes(new String[]{"堂食", "外带", "定时达"})  // 样例场景
                .processorIds(new int[]{0})  // 样例处理器ID
                .build();
        processorMapRepository.save(processorMap);

    }

    @Autowired
    private OrderRepository orderRepository;  // 假设你有一个 UserRepository


    public void createNewOrder() {

        Order order = Order.builder()
                .id(new ObjectId())  // 自动生成的ObjectId
                .userId(new ObjectId("66f157e249b56d25d48bf329")) //lemo
                .state("已支付")
                .customerType("未认证为学生身份的用户业务")
                .scene("堂食")
                .items(createDefaultOrderItems())
                .build();

        orderRepository.save(order);

    }

    // 创建默认的OrderItem列表方法
    private List<OrderItem> createDefaultOrderItems() {
        return Arrays.asList(
                OrderItem.builder()
                        .productId(new ObjectId("66f224e960315e7ec0d02c91"))  // 设置默认的ProductId
                        .optionValues(createDefaultOptionValues1())  // 设置默认OptionValues
                        .price(20)  // 设置默认价格
                        .build(),
                OrderItem.builder()
                        .productId(new ObjectId("66f226fc60315e7ec0d02c92"))  // 设置默认的ProductId
                        .optionValues(createDefaultOptionValues2())  // 设置默认OptionValues
                        .price(21)  // 设置默认价格
                        .build()
        );
    }

    // 创建默认的OptionValues方法
    private Map<String, OptionValue> createDefaultOptionValues1() {
        Map<String, OptionValue> optionValues = new HashMap<>();

        // 为 "Color" 选项创建一个列表
        optionValues.put("规格",
                OptionValue.builder()
                        .uuid("1727145031681")
                        .value("中杯")
                        .priceAdjustment(0)  // 价格调整
                        .build()
        );

        return optionValues;
    }

    // 创建默认的OptionValues方法
    private Map<String, OptionValue> createDefaultOptionValues2() {
        Map<String, OptionValue> optionValues = new HashMap<>();

        // 为 "Color" 选项创建一个列表
        optionValues.put("温度",
                OptionValue.builder()
                        .uuid("1727145026950")
                        .value("热")
                        .priceAdjustment(2)  // 价格调整
                        .build()
        );

        // 为 "Size" 选项创建一个列表
        optionValues.put("Size",
                OptionValue.builder()
                        .uuid("1727145031681")
                        .value("中杯")
                        .priceAdjustment(2)  // 价格减少
                        .build()
        );

        return optionValues;
    }

//    @Autowired
//    private UserRepository userRepository;  // 假设你有一个 UserRepository
//
//    public void createNewUser() {
//        // 原始密码
//        String rawPassword = "lemopwd";
//        // 对密码进行哈希
//        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
//
//        // 构建新的用户
//        User user = User.builder()
//                .id(new ObjectId())  // 自动生成的ObjectId
//                .openid("someOpenId")  // 可以是随机生成的值或真实值
//                .sessionKey("someSessionKey")  // 假设是微信的 sessionKey
//                .name("lemo")
//                .hashedPassword(hashedPassword)
//                .school("Some School")
//                .role("user")  // 可以根据你的需求设置角色
//                .jwt("someJwtToken")  // 这里你可以设置空值，后面可以动态生成
//                .expend(0.0)  // 初始花费
//                .orderNum(0)  // 初始订单数量
//                .address("Some Address")
//                .createdAt(new Date())  // 自动生成当前时间
//                .build();
//
//        // 保存用户到 MongoDB
//        userRepository.save(user);
//
//        System.out.println("User created with name: " + user.getName() + " and hashed password: " + user.getHashedPassword());
//    }
}
