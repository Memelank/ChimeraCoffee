package com.chimera.weapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.chimera.weapp.vo.OptionValue;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

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
    private double price;
    private String describe;
    private String short_desc;
    private int status; // 0是下架，1是上架
    private int delete; // 1是删除，0是正常
    private Map<String, List<OptionValue>> productOptions; // String为option的objectId
}
