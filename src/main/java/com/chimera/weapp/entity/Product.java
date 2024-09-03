package com.chimera.weapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import org.bson.types.ObjectId;

@Document(collection = "product")
@Data  // Lombok注解，自动生成所有属性的getter和setter方法，以及toString、equals、hashCode方法
@NoArgsConstructor  // Lombok注解，自动生成无参构造函数
@AllArgsConstructor  // Lombok注解，自动生成全参构造函数
@Builder  // Lombok注解，提供构建器模式的支持
public class Product {

    @Id
    private ObjectId id;
    private Integer cid = 0;    //分类，关联product_cate表，0为待分类
    private String title;
    private String img; //图片url
    private Integer price;
    private String desc;    //描述
    private Integer status = 0; //状态 -1为删除，0为下架，1为上架
    private LocalDate add_date; //添加日期
    //是否需要一个ord表示显示的顺序

}
