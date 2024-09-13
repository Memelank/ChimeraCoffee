package com.chimera.weapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    private ObjectId id;
    private ObjectId cateId;
    private String name;
    private String imgURL;
    private String describe;
    private int status; // 0是下架，1是上架
    private List<ObjectId> productOptionIds;
}
