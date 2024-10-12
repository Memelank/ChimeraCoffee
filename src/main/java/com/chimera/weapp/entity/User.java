package com.chimera.weapp.entity;
import com.chimera.weapp.vo.CouponIns;
import com.chimera.weapp.vo.PointsProductIns;
import io.swagger.v3.oas.annotations.media.Schema;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Date;
import java.util.List;


@Document(collection = "user")
@Data  // Lombok注解，自动生成所有属性的getter和setter方法，以及toString、equals、hashCode方法
@NoArgsConstructor  // Lombok注解，自动生成无参构造函数
@AllArgsConstructor  // Lombok注解，自动生成全参构造函数
@Builder  // Lombok注解，提供构建器模式的支持
public class User {

    @Id
    @Schema(description = "Order中的UserId是这个吗？")
    private ObjectId id;
    private String openid;
    private String sessionKey;
    private String name;
    @Schema(description = "学生认证后设为True")
    private Boolean studentCert;
    @Schema(description = "学生认证结果")
    private String school;
    @Schema(description = "总消费金额")
    private double expend;
    @Schema(description = "下单次数")
    private int orderNum;
    @Schema(description = "持有积分")
    private int points;

    @Schema(description = "用户持有的优惠券实例")
    private List<CouponIns> coupons;

    @Schema(description = "用户兑换过的积分商品列表")
    private List<PointsProductIns> pointsProducts;

    @CreatedDate  // 自动填充创建时间
    private Date createdAt;

    // 后端管理账号用
    private String hashedPassword;
    private String role;
    private String jwt;
}
