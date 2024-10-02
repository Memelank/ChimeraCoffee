package com.chimera.weapp.entity;

import com.chimera.weapp.vo.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    private ObjectId id;
    private ObjectId userId;
    private String state;
    private String customerType;
    private String scene;
    private List<OrderItem> items;
    private int orderNum;

    @CreatedDate  // 自动填充创建时间
    private Date createdAt;
}
