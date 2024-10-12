package com.chimera.weapp.entity;

import com.chimera.weapp.vo.OptionValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "points_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsProduct {
    @Id
    private ObjectId id;
    private String name;
    private String imgURL;
    @Schema(description = "所需积分")
    private int costPoints;
    @Schema(description = "描述")
    private String describe;
    @Schema(description = "status=0为下架，后端不返回给前端")
    private int status; // 0是下架，1是上架
    @Schema(description = "对于delete=1的，后端不返回")
    private int delete; // 1是删除，0是正常
    @Schema(description = "已兑换数量")
    private int redeemedNum;
}
