package com.chimera.weapp.entity;

import com.chimera.weapp.vo.OptionValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "product_option")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOption {
    @Id
    private ObjectId id;
    @Schema(description = "商品的可选项名称，如\"规格\"")
    private String name;
    @Schema(description = "对于一个可选项，所有的可能选值，商铺管理用")
    private List<OptionValue> values;
}
