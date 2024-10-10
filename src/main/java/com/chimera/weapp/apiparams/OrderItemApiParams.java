package com.chimera.weapp.apiparams;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemApiParams {
    @Schema(description = "Product.id")
    @NotNull
    private ObjectId productId;
    @Schema(description = "商品选项，key为ProductOption.id, value为OptionValue.uuid")
    private Map<String, String> optionValues;
}
