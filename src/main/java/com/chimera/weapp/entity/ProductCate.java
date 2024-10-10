package com.chimera.weapp.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "status=0为下架，后端不返回给前端")
    private Integer status; //状态，0为下架，1为上架
    @Schema(description = "优先级，便于设置显示顺序，已自动排序")
    private Integer priority;
    @Schema(description = "对于delete=1的，后端不返回")
    private int delete; // 1是删除，0是正常

    @Override
    public int compareTo(ProductCate o) {
        return this.priority - o.priority;
    }
}
