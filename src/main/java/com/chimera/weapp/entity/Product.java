package com.chimera.weapp.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "对应ProductCate.id")
    private ObjectId cateId;
    private String name;
    private String imgURL;
    @Schema(description = "基础价格，单位为分")
    private int price;
    @Schema(description = "学生优惠价。若用户为已认证学生，展示基础价格，然后划掉，再展示学生优惠价。若未认证则正常展示基础价格。单位为分。")
    private int stuPrice;
    @Schema(description = "详情页描述")
    private String describe;
    @Schema(description = "菜单列表中，name下面展示的简介")
    private String short_desc;
    @Schema(description = "status=0为下架，后端不返回给前端")
    private int status; // 0是下架，1是上架
    @Schema(description = "对于delete=1的，后端不返回")
    private int delete; // 1是删除，0是正常
    @Schema(description = "加购商品的可选项, key=ProductOption.id")
    private Map<String, List<OptionValue>> productOptions; // String为option的objectId
}
