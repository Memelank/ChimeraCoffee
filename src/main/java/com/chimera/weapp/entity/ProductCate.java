package com.chimera.weapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "product_cate")
@Data  // Lombok注解，自动生成所有属性的getter和setter方法，以及toString、equals、hashCode方法
@NoArgsConstructor  // Lombok注解，自动生成无参构造函数
@AllArgsConstructor  // Lombok注解，自动生成全参构造函数
@Builder  // Lombok注解，提供构建器模式的支持
public class ProductCate implements Comparable<ProductCate> {

    @Id
    private ObjectId id;
    private String title;
    private Integer status; //状态，0为禁用，1为正常
    private LocalDate add_date; //添加日期
    private Integer priority;

    @Override
    public int compareTo(ProductCate o) {
        return this.priority - o.priority;
    }
}
