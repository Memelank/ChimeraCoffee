package com.chimera.weapp.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Document(collection = "user")
@Data  // Lombok注解，自动生成所有属性的getter和setter方法，以及toString、equals、hashCode方法
@NoArgsConstructor  // Lombok注解，自动生成无参构造函数
@AllArgsConstructor  // Lombok注解，自动生成全参构造函数
@Builder  // Lombok注解，提供构建器模式的支持
public class User {

    @Id
    private String uid;
    private String name;
    private String school;
}
