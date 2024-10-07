package com.chimera.weapp.entity;
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
    private String hashedPassword;
    private String school;
    private String role;
    private String jwt;
    private double expend;
    private int orderNum;
    private String address;

    @CreatedDate  // 自动填充创建时间
    private Date createdAt;
}
